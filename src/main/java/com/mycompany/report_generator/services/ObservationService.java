package com.mycompany.report_generator.services;

import com.mycompany.report_generator.dto.ObservationRequestDTO;
import com.mycompany.report_generator.models.Doctor;
import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;
import com.mycompany.report_generator.models.Patient;
import java.util.List;

public interface ObservationService {

    Observation recordNewObservation(Patient patient, Doctor doctor, ObservationRequestDTO request);

    ObservationReport getGeneratedReport(Long observationId);

    List<Observation> getObservationsByPatientId(Long patientId);

    Observation saveObservation(Observation observation);

    void deleteReportByObservationId(Long observationId);

    Observation getObservationById(Long observationId);

    List<Observation> getObservationsByDoctorId(Long doctorId);

    void deleteObservation(Long observationId);
}