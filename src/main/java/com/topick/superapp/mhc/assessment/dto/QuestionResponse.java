package com.topick.superapp.mhc.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;
@Data
@AllArgsConstructor
public class QuestionResponse {
    private UUID questionId;
    private String questionText;
    private Integer  questionOrder;
    private Integer minScore;
    private Integer maxScore;

}
