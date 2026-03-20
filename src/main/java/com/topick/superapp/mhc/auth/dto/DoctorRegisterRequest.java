package com.topick.superapp.mhc.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorRegisterRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String fullName;
    private String phone;
    @NotBlank
    private String inviteCode;
    private String specialty;
    @NotBlank
    private String licenseNumber;
    @NotNull
    private BigDecimal pricePerSession;
}
