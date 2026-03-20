package com.topick.superapp.mhc.assessment.dto;

import com.topick.superapp.mhc.model.Questionnaire;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class QuestionnaireResponse {

    private String code;
    private String name;
    private String description;
    private List<QuestionResponse> questions;


}
