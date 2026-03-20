package com.topick.superapp.mhc.assessment.service;

import com.topick.superapp.mhc.ApiResponse;
import com.topick.superapp.mhc.Util.SecurityUtils;
import com.topick.superapp.mhc.assessment.dto.*;
import com.topick.superapp.mhc.assessment.repository.AssessmentAnswerRepository;
import com.topick.superapp.mhc.assessment.repository.AssessmentRepository;
import com.topick.superapp.mhc.assessment.repository.QuestionRepository;
import com.topick.superapp.mhc.assessment.repository.QuestionnaireRepository;
import com.topick.superapp.mhc.auth.repository.PatientRepository;
import com.topick.superapp.mhc.enums.Role;
import com.topick.superapp.mhc.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.UUIDEditor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;
    public ApiResponse getQuestionsByCode(String code) {
        if (code == null || code.isEmpty()) {
            return new ApiResponse("input null", false, null);
        }
        Optional<Questionnaire> questionnaire = questionnaireRepository.findByCode(code);
        List<Question> questions = questionRepository.findByQuestionnaireCodeOrderByQuestionOrderAsc(code);
        if (questions.isEmpty() || !questionnaire.isPresent()) {
            return new ApiResponse("ko tìm thấy code", false, null);
        }
        List<QuestionResponse> questionResponses = questions.stream()
                .map(q -> new QuestionResponse(
                        q.getId(),
                        q.getQuestionText(),
                        q.getQuestionOrder(),
                        q.getMinScore(),
                        q.getMaxScore()

                        )

                ).collect(Collectors.toList());

        return new ApiResponse(
                "Lấy questions thành công",
                true,
                new QuestionnaireResponse(
                        questionnaire.get().getCode(),
                        questionnaire.get().getName(),
                        questionnaire.get().getDescription(),
                        questionResponses
                )
        );
    }

    public ApiResponse submitAssessment(SubmitRequest  submitRequest) {
        if (submitRequest == null) {
            return new ApiResponse("input null", false, null);
        }
        UUID patientId =  (UUID) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        if(patientId == null){
            return new ApiResponse("input null", false, null);
        }
        Optional<Patient> patient = patientRepository.findById(patientId);
        if(!patient.isPresent()){
            return new ApiResponse("patient ko tồn tại", false, null);
        }
        Optional<Questionnaire> questionnaire = questionnaireRepository.findByCode(submitRequest.getQuestionnaireCode());
        if(!questionnaire.isPresent()){
            return new ApiResponse("ko tìm thấy questionnaire", false, null);
        }


        int totalScore = submitRequest.getAnswers().stream()
                .mapToInt(a -> a.getScore())
                .sum();
        String severity = mapSeverity(totalScore);
        Assessment assessment = Assessment.builder()
                .patient(patient.get())
                .questionnaire(questionnaire.get())
                .createdAt(LocalDateTime.now())
                .totalScore(totalScore)
                .severity(severity)
                .build();
        assessmentRepository.save(assessment);
        for(var answer : submitRequest.getAnswers()){
            Question question = questionRepository.findById(answer.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question không tồn tại"));
            AssessmentAnswer assessmentAnswer = AssessmentAnswer.builder()
                    .question(question)
                    .score(answer.getScore())
                    .assessment(assessment)
                    .build();
            assessmentAnswerRepository.save(assessmentAnswer);
        }
       return new ApiResponse(
               "Submit thành công", true, "200"
       );
    }
    private String mapSeverity(int score) {
        if (score <= 4) return "Minimal";
        if (score <= 9) return "Mild";
        if (score <= 14) return "Moderate";
        if (score <= 19) return "Moderately Severe";
        return "Severe";
    }

   public ApiResponse getAssessmentsByPatient() {
       UUID userId = SecurityUtils.getCurrentUserId();
       if (userId == null) {
        return  new ApiResponse("input null", false, null);
       }
       Role role = SecurityUtils.getCurrentRole();
       if (role == null) {
           return new ApiResponse("input null", false, null);
       }
       if(role == Role.PATIENT){
           Assessment assessment = assessmentRepository.findTopByPatientIdOrderByCreatedAtDesc(userId);
           if (assessment == null) {
               return new ApiResponse("No assessment found", false, null);
           }
           LatestSubmitResponse latest = LatestSubmitResponse.builder()
                   .assessmentId(assessment.getId())
                   .totalScore(assessment.getTotalScore())
                   .severity(assessment.getSeverity())
                   .submittedAt(assessment.getCreatedAt())
                   .answersResponseList(assessment.getAnswers().stream()
                           .map(answer -> AnswersResponse.builder()
                                   .questionId(answer.getQuestion().getId())
                                   .questionText(answer.getQuestion().getQuestionText())
                                   .score(answer.getScore())
                                   .build())
                           .toList()
                   )
                   .build();
           List<HistorySubmitResponse> history = assessmentRepository
                   .findTop10ByPatientIdOrderByCreatedAtDesc(userId)
                   .stream()
                   .map(a -> HistorySubmitResponse.builder()
                           .assessmentId(a.getId())
                           .totalScore(a.getTotalScore())
                           .severity(a.getSeverity())
                           .submittedAt(a.getCreatedAt())
                           .build())
                   .toList();
           AssessmentResultResponse assessmentResultResponse = AssessmentResultResponse.builder()
                   .latestSubmitResponse(latest)
                   .historySubmitResponseList(history)
                   .build();
           return new ApiResponse("success", true, assessmentResultResponse);
       }
       return new ApiResponse("error", false, null);
    }
//       }else if(role == Role.DOCTOR){
//
//       }
   }


