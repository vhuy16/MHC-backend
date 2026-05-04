package com.topick.superapp.mhc.doctor.Dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class DoctorResponse {
    private UUID id;
    private String fullName;
    private String specialty;
    private String bio;
    private BigDecimal pricePerSession;
    private BigDecimal rating;
}
