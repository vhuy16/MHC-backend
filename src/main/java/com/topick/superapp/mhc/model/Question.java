package com.topick.superapp.mhc.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @SequenceGenerator(name = "questions_id_gen", sequenceName = " ")
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;

    @Size(max = 500)
    @NotNull
    @Nationalized
    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @NotNull
    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @NotNull
    @Column(name = "min_score", nullable = false)
    private Integer minScore;

    @NotNull
    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

}