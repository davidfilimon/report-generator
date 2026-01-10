package com.mycompany.report_generator.services;

import com.google.gson.Gson;
import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;
import com.mycompany.report_generator.repositories.ObservationReportRepository;
import com.mycompany.report_generator.security.LLMGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class LLMReportGenerationService implements ReportGenerationService {

    private final LLMClient llmClient;
    private final ObservationReportRepository reportRepository;
    private final Gson gson = new Gson();

    private static class LLMResponse {
        String diagnosis;
        String riskLevel;
        String analysis;
    }

    @Override
    @Transactional
    public ObservationReport generateReport(Observation observation) {
        try {
            validateObservation(observation);

            if (observation.getId() != null) {
                reportRepository.deleteByObservationId(observation.getId());
            }

            String inputPrompt = buildPromptFromObservation(observation);
            String jsonOutput = llmClient.generateReport(inputPrompt);

            LLMResponse llmResponse = parseResponse(jsonOutput);
            return saveReport(observation, llmResponse);

        } catch (Exception e) {
            log.error("Failed to generate medical report for observation {}: {}",
                    observation.getId(), e.getMessage());
            return createErrorReport(observation, e.getMessage());
        }
    }

    private void validateObservation(Observation observation) {
        if (observation.getPatient() == null || observation.getPatient().getBirthDate() == null) {
            throw new IllegalArgumentException("Observation missing critical patient data.");
        }
    }

    private LLMResponse parseResponse(String jsonOutput) {
        if (jsonOutput == null || jsonOutput.startsWith("Eroare")) {
            throw new LLMGenerationException("LLM Client connection error: " + jsonOutput);
        }

        LLMResponse response = gson.fromJson(jsonOutput, LLMResponse.class);
        if (response == null || response.analysis == null || response.diagnosis == null) {
            throw new LLMGenerationException("Incomplete JSON response from LLM.");
        }
        return response;
    }

    private ObservationReport saveReport(Observation observation, LLMResponse response) {
        String patientName = String.format("%s %s",
                observation.getPatient().getFirstName(),
                observation.getPatient().getLastName());

        String doctorName = observation.getDoctor() != null ?
                String.format("%s %s", observation.getDoctor().getFirstName(), observation.getDoctor().getLastName()) :
                "Unknown Doctor";

        ObservationReport report = ObservationReport.builder()
                .observation(observation)
                .reportContent(response.analysis)
                .patientName(patientName)
                .doctorName(doctorName)
                .potentialDiagnosis(response.diagnosis)
                .riskLevel(response.riskLevel)
                .generationDate(LocalDateTime.now())
                .build();

        return reportRepository.save(report);
    }

    private String buildPromptFromObservation(Observation observation) {
        var patient = observation.getPatient();
        int age = Period.between(patient.getBirthDate(), LocalDate.now()).getYears();
        String gender = "M".equalsIgnoreCase(patient.getGender()) ? "masculin" : "feminin";
        String tensiune = observation.getVitalSigns().getOrDefault("Tensiune Arterială", "N/A");

        return String.format("""
                ROL: Ești un medic specialist cardiolog cu experiență vastă în diagnosticarea patologiilor cardiovasculare.
                Analizează cazul medical și furnizează răspunsul în format JSON.
                Răspunde exclusiv în limba română. Diagnoza și analiza trebuie să fie concise.
                
                Date Pacient:
                - Vârstă: %d ani
                - Sex: %s
                - Tensiune Arterială: %s
                - Colesterol: %s
                - Fumător: %s
                - Simptome Acute: %s
                - Istoric Medical: %s
                """,
                age, gender, tensiune,
                patient.getCholesterolStatus(),
                patient.isSmoker() ? "da" : "nu",
                observation.getSymptomsDescription(),
                patient.getMedicalHistory());
    }

    private ObservationReport createErrorReport(Observation observation, String errorMsg) {
        return ObservationReport.builder()
                .observation(observation)
                .patientName("Error")
                .doctorName("System")
                .reportContent("System was unable to generate report: " + errorMsg)
                .potentialDiagnosis("Error during processing")
                .riskLevel("N/A")
                .generationDate(LocalDateTime.now())
                .build();
    }
}