package com.topick.superapp.mhc.booking.BookingService;

import com.topick.superapp.mhc.ApiResponse;
import com.topick.superapp.mhc.booking.BookingRepository.BookingRepository;
import com.topick.superapp.mhc.booking.BookingRepository.PaymentRepository;
import com.topick.superapp.mhc.doctorAvailability.Repository.DoctorAvailabilityRepository;
import com.topick.superapp.mhc.enums.BookingStatus;
import com.topick.superapp.mhc.enums.PaymentStatus;
import com.topick.superapp.mhc.model.Booking;
import com.topick.superapp.mhc.model.DoctorAvailability;
import com.topick.superapp.mhc.model.Payment;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;

@Service
@Transactional
public class PaymentService {
    @Autowired
    private PayOS payOS;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    public ApiResponse handleWebhook(String body){
        WebhookData data = payOS.webhooks().verify(body);
        Payment payment = paymentRepository.findByTransactionId(String.valueOf(data.getOrderCode()))
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        if(data.getCode().equals("00")){

            payment.setStatus(PaymentStatus.PAID);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Booking booking = payment.getBooking();
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setPaymentStatus(PaymentStatus.PAID);
            bookingRepository.save(booking);
        }else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            Booking booking = payment.getBooking();
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            // set slot về AVAILABLE

            booking.getAvailability().setStatus("AVAILABLE");
            doctorAvailabilityRepository.save(booking.getAvailability());
        }
        return new ApiResponse("Webhook processed", true, null);
    }
}
