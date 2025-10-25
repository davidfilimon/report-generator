package com.mycompany.report_generator.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "observations")
@Data
@NoArgsConstructor
public class Observation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDateTime observationDate = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String symptomsDescription;

    @ElementCollection
    @CollectionTable(name = "observation_vital_signs",
            joinColumns = @JoinColumn(name = "observation_id"))
    @MapKeyColumn(name = "sign_name")
    @Column(name = "sign_value")
    private Map<String, String> vitalSigns;

    public Observation(Patient patient, Doctor doctor, String symptomsDescription, Map<String, String> vitalSigns) {
        this.patient = patient;
        this.doctor = doctor;
        this.symptomsDescription = symptomsDescription;
        this.vitalSigns = vitalSigns;
    }

    public String getSymptomsDescription() {
        return symptomsDescription;
    }

}