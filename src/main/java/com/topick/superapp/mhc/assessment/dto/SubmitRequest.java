package com.topick.superapp.mhc.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.rmi.server.UID;
import java.util.List;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitRequest {
    private String questionnaireCode ;
    List<AnswerRequest> answers;
}
