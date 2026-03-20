package com.topick.superapp.mhc.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LatestSubmitResponse {
    private UUID assessmentId;
    private int totalScore;
    private LocalDateTime submittedAt;

    private String severity;
    List<AnswersResponse> answersResponseList;

}
