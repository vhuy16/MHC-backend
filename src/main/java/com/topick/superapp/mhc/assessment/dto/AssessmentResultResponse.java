package com.topick.superapp.mhc.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AssessmentResultResponse {
    private LatestSubmitResponse latestSubmitResponse;
    List<HistorySubmitResponse> historySubmitResponseList;
}
