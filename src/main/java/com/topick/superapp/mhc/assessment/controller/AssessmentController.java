package com.topick.superapp.mhc.assessment.controller;

import com.topick.superapp.mhc.ApiResponse;
import com.topick.superapp.mhc.assessment.dto.SubmitRequest;
import com.topick.superapp.mhc.assessment.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentController {
    private final AssessmentService assessmentService;

    @GetMapping("/questionnaire")
    public ResponseEntity<ApiResponse> getByCode(@RequestParam String code) {
        ApiResponse response = assessmentService.getQuestionsByCode(code);
        if(response.isSuccess()){
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse> submitAssessment(@RequestBody SubmitRequest  submitRequest) {
        ApiResponse response = assessmentService.submitAssessment(submitRequest);
        if(response.isSuccess()){
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/getAssessments")
    public ResponseEntity<ApiResponse> getAssessmentsByPatient() {
        ApiResponse response = assessmentService.getAssessmentsByPatient();
        if(response.isSuccess()){
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
    @GetMapping("/getAllQuestionnaires")
    public ResponseEntity<ApiResponse> getAllQuestionnaires() {
        ApiResponse response = assessmentService.getAllQuestionnaires();
        if(response.isSuccess()){
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
