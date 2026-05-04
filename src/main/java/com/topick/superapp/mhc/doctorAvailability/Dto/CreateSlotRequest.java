package com.topick.superapp.mhc.doctorAvailability.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class CreateSlotRequest {
    @NotEmpty
    private List<LocalDate> dates;

    @NotNull
    private LocalTime startOfDay;

    @NotNull
    private LocalTime endOfDay;

    // Thời lượng mỗi slot (ví dụ: 30 phút)
    private int slotDurationMinutes = 60;
}
