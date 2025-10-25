package com.mycompany.report_generator.services;


import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;
import com.mycompany.report_generator.services.ReportGenerationService;
import com.mycompany.report_generator.services.RiskEvaluationStrategy;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class ReportGenerationServiceImpl implements ReportGenerationService {

    private final List<RiskEvaluationStrategy> strategies;

    public ReportGenerationServiceImpl(List<RiskEvaluationStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public ObservationReport generateReport(Observation observation) {

        List<String> allRiskFactors = new ArrayList<>();

        String potentialDiagnosis = "NOT YET IMPLEMENTED";

        return ObservationReport.builder()
                .patientName("Placeholder Name")
                .doctorName("Placeholder Doctor")
                .observationSummary(observation.getSymptomsDescription())
                .potentialDiagnosis(potentialDiagnosis)
                .riskFactors(allRiskFactors)
                .build();
    }

    private String determineDiagnosis(List<String> risks) {
        return "Not Implemented";
    }
}
