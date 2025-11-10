package com.mycompany.report_generator.services;

import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReportGenerationServiceImpl implements ReportGenerationService {

    private final List<RiskEvaluationStrategy> strategies;

    public ReportGenerationServiceImpl(List<RiskEvaluationStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public ObservationReport generateReport(Observation observation) {
        List<String> allRiskFactors = new ArrayList<>();

        // folosim strategiile injectate
        for (RiskEvaluationStrategy strategy : strategies) {
            allRiskFactors.addAll(strategy.evaluate(
                    observation.getVitalSigns(),
                    observation.getSymptomsDescription()
            ));
        }

        String potentialDiagnosis = determineDiagnosis(allRiskFactors);

        return ObservationReport.builder()
                .patientName(
                        observation.getPatient() != null
                                ? observation.getPatient().getFirstName() + " " + observation.getPatient().getLastName()
                                : "Placeholder Name")
                .doctorName(
                        observation.getDoctor() != null
                                ? observation.getDoctor().getFirstName() + " " + observation.getDoctor().getLastName()
                                : "Placeholder Doctor")
                .observationSummary(observation.getSymptomsDescription())
                .potentialDiagnosis(potentialDiagnosis)
                .riskFactors(allRiskFactors)
                .build();
    }


    private String determineDiagnosis(List<String> risks) {
        if(risks.contains("High Blood Pressure") && risks.contains("High Heart Rate")) return "Cardiovascular Risk";
        if(risks.contains("High Blood Pressure")) return "Hypertension Risk";
        if(risks.contains("High Heart Rate")) return "Cardiac Risk";
        return "Normal";
    }
}
