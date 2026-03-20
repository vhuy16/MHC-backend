package com.topick.superapp.mhc.assessment.repository;

import com.topick.superapp.mhc.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByQuestionnaireCodeOrderByQuestionOrderAsc(String code);
}
