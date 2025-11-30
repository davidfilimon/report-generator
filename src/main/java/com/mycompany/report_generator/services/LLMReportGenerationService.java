package com.mycompany.report_generator.services;

import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;
import com.mycompany.report_generator.repositories.ObservationReportRepository;
import com.mycompany.report_generator.services.LLMClient; // Am presupus că LLMClient se află în 'utils'
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.gson.Gson; // NOU: Import pentru parsarea JSON

@Primary
@Service
public class LLMReportGenerationService implements ReportGenerationService {

    private final LLMClient llmClient;
    private final ObservationReportRepository reportRepository;
    private final Gson gson = new Gson(); // Instanță Gson

    private static class LLMResponse {
        String diagnosis; // Diagnoză potențială
        String risk;      // Nivel de risc (Scăzut, Mediu, Înalt, Critic)
        String analysis;  // Analiza explicativă (devine reportContent)
    }

    public LLMReportGenerationService(
            LLMClient llmClient,
            ObservationReportRepository reportRepository
    ) {
        this.llmClient = llmClient;
        this.reportRepository = reportRepository;
    }

    @Override
    @Transactional
    public ObservationReport generateReport(Observation observation) {

        if (observation.getId() != null) {
            reportRepository.deleteByObservationId(observation.getId());
        }

        String inputPrompt = buildPromptFromObservation(observation);

        if (inputPrompt.startsWith("Observation missing")) {
            return ObservationReport.builder()
                    .patientName("Eroare de Date")
                    .doctorName("Sistem")
                    .reportContent(inputPrompt)
                    .potentialDiagnosis("Date Pacient Incomplete")
                    .riskLevel("N/A")
                    .generationDate(LocalDateTime.now())
                    .build();
        }

        String llmOutput = llmClient.generateReport(inputPrompt);

        LLMResponse llmResponse;

        try {
            // Încercăm să extragem și să mapăm răspunsul JSON
            // Uneori, LLM adaugă text suplimentar. Încercăm să izolăm obiectul JSON.
            int start = llmOutput.indexOf('{');
            int end = llmOutput.lastIndexOf('}');
            String jsonContent = (start != -1 && end != -1) ? llmOutput.substring(start, end + 1) : llmOutput;

            llmResponse = gson.fromJson(jsonContent, LLMResponse.class);

        } catch (Exception e) {
            System.err.println("Eroare la parsarea răspunsului JSON de la LLM. Răspuns brut: " + llmOutput);
            // În caz de eroare de parsare, returnăm conținutul brut, dar cu eroare vizibilă
            return ObservationReport.builder()
                    .observation(observation)
                    .patientName("Eroare la Parsare")
                    .doctorName("Sistem")
                    .reportContent("Eroare la parsarea răspunsului LLM. Asigură-te că Ollama returnează JSON. Răspuns brut: " + llmOutput)
                    .potentialDiagnosis("Eroare la Parsare")
                    .riskLevel("Critic")
                    .generationDate(LocalDateTime.now())
                    .build();
        }


        // Extrage Nume Pacient & Doctor
        String patientFullName = observation.getPatient() != null
                ? observation.getPatient().getFirstName() +
                " " +
                observation.getPatient().getLastName()
                : "Pacient Necunoscut";

        String doctorFullName = observation.getDoctor() != null
                ? observation.getDoctor().getFirstName() +
                " " +
                observation.getDoctor().getLastName()
                : "Doctor Necunoscut";

        // Creează entitatea ObservationReport folosind datele structurate
        ObservationReport report = ObservationReport.builder()
                .observation(observation)
                .reportContent(llmResponse.analysis) // Conținutul analizei structurate
                .patientName(patientFullName)
                .doctorName(doctorFullName)
                .potentialDiagnosis(llmResponse.diagnosis) // Diagnoza structurată
                .riskLevel(llmResponse.risk) // Nivelul de risc structurat
                .generationDate(LocalDateTime.now())
                .build();

        return reportRepository.save(report);
    }

    // --- Logica de Construire Prompt - Actualizată pentru a cere JSON ---

    private String buildPromptFromObservation(Observation observation) {
        int age = 0;
        String gender = "necunoscut";
        String smokerStatus = "necunoscut";
        String cholesterolStatus = "necunoscut";
        String medicalHistory = "";

        if (
                observation.getPatient() == null ||
                        observation.getPatient().getBirthDate() == null
        ) {
            return "Observation missing patient data. Cannot generate report.";
        }

        // Extrage datele Pacientului
        age = calculateAge(observation.getPatient().getBirthDate());

        // Previne NullPointerException
        if (observation.getPatient().getGender() != null) {
            gender = observation.getPatient().getGender().equalsIgnoreCase("M")
                    ? "masculin"
                    : "feminin";
        }
        smokerStatus = observation.getPatient().isSmoker() ? "da" : "nu";

        if (observation.getPatient().getCholesterolStatus() != null) {
            cholesterolStatus = observation.getPatient().getCholesterolStatus();
        }
        if (observation.getPatient().getMedicalHistory() != null) {
            medicalHistory = observation.getPatient().getMedicalHistory();
        }

        // Extrage Date Vitale
        String tensiune = observation
                .getVitalSigns()
                .getOrDefault("Tensiune Arterială", "N/A");

        // Construiește promptul Llama3 Chat, dar cu instrucțiuni stricte de JSON
        StringBuilder sb = new StringBuilder();

        sb.append("<|system|>\n");
        sb.append(
                "Ești un sistem expert medical AI specializat în analiza riscului cardiovascular. " +
                        "Trebuie să generezi o analiză concisă în format JSON. " +
                        "Analiza ta va include un diagnostic potențial (diagnosis), un nivel de risc (risk: Scăzut/Mediu/Înalt/Critic) și o analiză explicativă (analysis). " +
                        "Răspunde exclusiv în limba română și furnizează DOAR obiectul JSON, fără explicații suplimentare în afara JSON-ului. \n"
        );
        sb.append("<|end|>\n");

        sb.append("<|user|>\n");
        sb.append("Analizează următorul caz medical și generează obiectul JSON:\n");
        sb.append("Vârstă: ").append(age).append(" ani, ");
        sb.append("Sex: ").append(gender).append(", ");
        sb.append("Tensiune Arterială: ").append(tensiune).append(" mmHg, ");
        sb.append("Colesterol: ").append(cholesterolStatus).append(", ");
        sb.append("Fumător: ").append(smokerStatus).append(".\n");

        sb
                .append("Simptome Acute: ")
                .append(observation.getSymptomsDescription())
                .append(".\n");
        sb
                .append("Istoric Medical: ")
                .append(medicalHistory)
                .append(".\n");
        sb.append("<|end|>\n");

        sb.append("<|assistant|>\n");
        // Aici LLM-ul va trebui să răspundă direct cu JSON

        return sb.toString();
    }

    private int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}