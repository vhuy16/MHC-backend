package com.topick.superapp.mhc.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AnswersResponse {
    private UUID questionId;
    private String questionText;
    private Integer score;
    private boolean isCrisisQuestion;
}
