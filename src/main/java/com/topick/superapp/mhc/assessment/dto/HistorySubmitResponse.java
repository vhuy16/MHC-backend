package com.topick.superapp.mhc.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HistorySubmitResponse {
    private UUID assessmentId;
    private int totalScore;
    private LocalDateTime submittedAt;

    private String severity;
}
