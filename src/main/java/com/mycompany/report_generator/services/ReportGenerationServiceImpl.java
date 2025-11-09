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
        Map<String, String> vitals = observation.getVitalSigns();

        if(vitals != null) {
            String bp = vitals.get("blood_pressure");
            if(bp != null && bp.matches("1[4-9]\\d/.*|2\\d{2}/.*")) {
                allRiskFactors.add("High Blood Pressure");
            }

            String hr = vitals.get("heart_rate");
            if(hr != null) {
                try {
                    int heartRate = Integer.parseInt(hr.replaceAll("\\D", ""));
                    if(heartRate > 100) {
                        allRiskFactors.add("High Heart Rate");
                    }
                } catch (NumberFormatException e) {
                    // ignore invalid input
                }
            }
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
