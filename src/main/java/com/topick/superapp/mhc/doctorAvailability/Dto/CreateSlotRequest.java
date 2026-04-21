package com.topick.superapp.mhc.doctorAvailability.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
public class CreateSlotRequest {
    @NotBlank
    private LocalDate date;
    @NotBlank
    private LocalTime startTime;
    @NotBlank
    private LocalTime endTime;
}
