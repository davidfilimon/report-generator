package com.mycompany.report_generator.controllers;

import com.mycompany.report_generator.services.LLMReportGenerationService;
import com.mycompany.report_generator.services.ReportGenerationService;
import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportGenerationService reportGenerationService;
    private final LLMReportGenerationService llmReportGenerationService;

    public ReportController(ReportGenerationService reportGenerationService,
                            LLMReportGenerationService llmReportGenerationService) {
        this.reportGenerationService = reportGenerationService;
        this.llmReportGenerationService = llmReportGenerationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ObservationReport> generateReport(@RequestBody Observation observation) {
        ObservationReport report = reportGenerationService.generateReport(observation);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/generate-llm")
    public ResponseEntity<ObservationReport> generateLLMReport(@RequestBody Observation observation) {
        ObservationReport report = llmReportGenerationService.generateReport(observation);
        return ResponseEntity.ok(report);
    }
}
