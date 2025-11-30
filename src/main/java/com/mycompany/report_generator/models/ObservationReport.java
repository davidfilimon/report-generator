package com.mycompany.report_generator.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObservationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "observation_id", unique = true)
    private Observation observation;

    private String patientName;
    private String doctorName;

    @Column(columnDefinition = "TEXT")
    private String reportContent;

    private String potentialDiagnosis;
    private String riskLevel;

    private LocalDateTime generationDate = LocalDateTime.now();
}
