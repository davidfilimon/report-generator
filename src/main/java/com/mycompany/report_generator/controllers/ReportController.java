package com.mycompany.report_generator.controllers;

import com.mycompany.report_generator.services.ReportGenerationService;
import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportGenerationService reportGenerationService;

    public ReportController(ReportGenerationService reportGenerationService) {
        this.reportGenerationService = reportGenerationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ObservationReport> generateReport(@RequestBody Observation observation) {

        ObservationReport report = reportGenerationService.generateReport(observation);

        return ResponseEntity.ok(report);
    }

}