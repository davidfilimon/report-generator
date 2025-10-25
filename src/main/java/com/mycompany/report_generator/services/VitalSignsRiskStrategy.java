package com.mycompany.report_generator.services;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Component
public class VitalSignsRiskStrategy implements RiskEvaluationStrategy {
    @Override
    public List<String> evaluate(Map<String, String> vitalSigns, String symptomsDescription) {
        // todo
        return Collections.emptyList();
    }
}