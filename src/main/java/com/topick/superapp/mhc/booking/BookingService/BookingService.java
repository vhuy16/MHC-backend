package com.topick.superapp.mhc.booking.BookingService;

import com.topick.superapp.mhc.ApiResponse;
import com.topick.superapp.mhc.Util.SecurityUtils;
import com.topick.superapp.mhc.auth.repository.PatientRepository;
import com.topick.superapp.mhc.booking.BookingRepository.BookingRepository;
import com.topick.superapp.mhc.booking.BookingRepository.PaymentRepository;
import com.topick.superapp.mhc.booking.Dto.CreateBookingRequest;
import com.topick.superapp.mhc.booking.Dto.CreatePaymentRequest;
import com.topick.superapp.mhc.doctorAvailability.Repository.DoctorAvailabilityRepository;
import com.topick.superapp.mhc.enums.BookingStatus;
import com.topick.superapp.mhc.enums.PaymentStatus;
import com.topick.superapp.mhc.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional

public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private PayOsService  payOsService;
    @Autowired
    private PaymentRepository paymentRepository;
    public ApiResponse CreateBooking(CreateBookingRequest createBookingRequest) {
        UUID patientID = SecurityUtils.getCurrentUserId();
        Patient patient = patientRepository.getReferenceById(patientID);
        int rows = doctorAvailabilityRepository.executeUpdate(createBookingRequest.getAvai_ID());


        if (rows ==  0) {
            return new ApiResponse("Slot đã được đặt", false, null);
        }
        DoctorAvailability doctorAvailability = doctorAvailabilityRepository
                .findById(createBookingRequest.getAvai_ID())
                .orElseThrow(() -> new RuntimeException("Availability not found"));
        BigDecimal amount = doctorAvailability.getDoctor().getPricePerSession();
        Booking booking = Booking.builder()
                .patient(patient)
                .availability(doctorAvailability)
                .doctor(doctorAvailability.getDoctor())
                .status(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        bookingRepository.save(booking);
        CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.builder()
                .amount(amount.longValue())
                .doctorName(doctorAvailability.getDoctor().getFullName())
                .build();

        CreatePaymentLinkResponse paymentResponse = payOsService.CreatePaymentLink(createPaymentRequest);

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(amount)
                .gateway("PAYOS")
                .transactionId(String.valueOf(paymentResponse.getOrderCode()))
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);
        return new ApiResponse("Tạo booking thành công", true, paymentResponse.getCheckoutUrl());
    }
}
