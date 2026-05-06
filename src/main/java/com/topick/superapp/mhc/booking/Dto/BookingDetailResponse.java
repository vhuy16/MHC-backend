package com.topick.superapp.mhc.booking.Dto;

import com.topick.superapp.mhc.enums.BookingStatus;
import com.topick.superapp.mhc.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class BookingDetailResponse {
    private UUID id;
    private UUID patientId;
    private String patientName;
    private String patientPhone; // Lấy thêm thông tin liên lạc
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private String note;
}