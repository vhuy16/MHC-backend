package com.topick.superapp.mhc.assessment.repository;

import com.topick.superapp.mhc.model.Assessment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
    @EntityGraph(attributePaths = {"answers", "answers.question"})
    Assessment findTopByPatientIdOrderByCreatedAtDesc(UUID userId);
    @EntityGraph(attributePaths = {"answers", "answers.question"})
    List<Assessment> findTop10ByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
