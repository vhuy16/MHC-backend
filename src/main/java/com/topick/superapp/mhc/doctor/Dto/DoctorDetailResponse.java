package com.topick.superapp.mhc.doctor.Dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DoctorDetailResponse {
    private UUID id;
    private String fullName;
    private String specialty;
    private String bio;
    private BigDecimal pricePerSession;
    private BigDecimal rating;
    private List<AvailabilityResponse> availabilities;
}

