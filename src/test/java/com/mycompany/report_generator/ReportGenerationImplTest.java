package com.mycompany.report_generator;

import com.mycompany.report_generator.models.Doctor;
import com.mycompany.report_generator.models.Observation;
import com.mycompany.report_generator.models.ObservationReport;
import com.mycompany.report_generator.models.Patient;
import com.mycompany.report_generator.repositories.ObservationReportRepository;
import com.mycompany.report_generator.services.LLMClient;
import com.mycompany.report_generator.services.ReportGenerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportGenerationServiceImplTest {

    private static final int NUMBER_OF_THREADS = 10;
    private static final int TOTAL_REQUESTS = 50;
    private static final int TIMEOUT_SECONDS = 10;

    @Mock
    private LLMClient mockLlmClient;

    @Mock
    private ObservationReportRepository mockReportRepository;

    @InjectMocks
    private ReportGenerationServiceImpl reportService;

    private final String MOCK_LLM_OUTPUT = "Raport detaliat generat de AI.";

    private Patient standardPatient;
    private Doctor standardDoctor;
    private Observation standardObservation;

    @BeforeEach
    void setUp() {
        standardPatient = new Patient();
        standardPatient.setFirstName("John");
        standardPatient.setLastName("Doe");

        standardDoctor = new Doctor();
        standardDoctor.setFirstName("Alice");
        standardDoctor.setLastName("Smith");

        standardObservation = new Observation(standardPatient, standardDoctor, "Fever", Map.of("temp", "39.5"));

        when(mockLlmClient.generateReport(anyString())).thenReturn(MOCK_LLM_OUTPUT);

        when(mockReportRepository.save(any(ObservationReport.class)))
                .thenAnswer(AdditionalAnswers.returnsFirstArg());
    }

    @Test
    void testGenerateReport_SuccessFullData() {
        ObservationReport report = reportService.generateReport(standardObservation);

        assertNotNull(report);
        assertEquals(MOCK_LLM_OUTPUT, report.getReportContent());
        assertEquals("John Doe", report.getPatientName());
        assertEquals("Alice Smith", report.getDoctorName());
        assertEquals("Default Diagnosis (din serviciul simplu)", report.getPotentialDiagnosis());

        verify(mockLlmClient, times(1)).generateReport("Generare raport simplu.");
        verify(mockReportRepository, times(1)).save(any(ObservationReport.class));
    }

    @Test
    void testGenerateReport_NullPatient() {
        Doctor doctor = new Doctor();
        doctor.setFirstName("Alice");
        doctor.setLastName("Smith");

        Observation observation = new Observation(null, doctor, "Headache", Map.of());

        ObservationReport report = reportService.generateReport(observation);

        assertNotNull(report);
        assertEquals("Pacient Necunoscut", report.getPatientName());
        assertEquals("Alice Smith", report.getDoctorName());

        verify(mockLlmClient, times(1)).generateReport(anyString());
        verify(mockReportRepository, times(1)).save(any(ObservationReport.class));
    }

    @Test
    void testGenerateReport_NullDoctor() {
        Patient patient = new Patient();
        patient.setFirstName("Jane");
        patient.setLastName("Doe");

        Observation observation = new Observation(patient, null, "Fever", Map.of());

        ObservationReport report = reportService.generateReport(observation);

        assertNotNull(report);
        assertEquals("Jane Doe", report.getPatientName());
        assertEquals("Doctor Necunoscut", report.getDoctorName());

        verify(mockLlmClient, times(1)).generateReport(anyString());
        verify(mockReportRepository, times(1)).save(any(ObservationReport.class));
    }


    @Test
    void testGenerateReport_StressTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        CountDownLatch latch = new CountDownLatch(TOTAL_REQUESTS);
        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger failureCounter = new AtomicInteger(0);

        System.out.println("--- Pornire Stress Test ---");
        System.out.printf("Număr de Fire de Execuție (Threads): %d\n", NUMBER_OF_THREADS);
        System.out.printf("Număr Total de Cereri: %d\n", TOTAL_REQUESTS);

        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            final int requestId = i;
            executor.submit(() -> {
                try {
                    ObservationReport report = reportService.generateReport(standardObservation);

                    assertNotNull(report, "Raportul nu ar trebui să fie null.");
                    assertEquals("John Doe", report.getPatientName(), "Numele pacientului este incorect.");

                    successCounter.incrementAndGet();
                } catch (Exception e) {
                    System.err.printf("Eroare la cererea #%d: %s\n", requestId, e.getMessage());
                    failureCounter.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean allFinished = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        executor.shutdownNow();

        System.out.println("--- Rezultate Stress Test ---");
        System.out.printf("Cereri Succes: %d\n", successCounter.get());
        System.out.printf("Cereri Eșuate: %d\n", failureCounter.get());
        System.out.printf("Timp Expirat (Timeout): %s\n", allFinished ? "Nu" : "Da");

        assertTrue(allFinished,
                String.format("Stress testul a expirat după %d secunde. Număr de cereri rămase: %d", TIMEOUT_SECONDS, latch.getCount()));

        assertEquals(TOTAL_REQUESTS, successCounter.get(),
                "Trebuie să existe un număr total de succese egal cu numărul total de cereri.");

        assertEquals(0, failureCounter.get(),
                "Nu ar trebui să existe erori în timpul execuției concurente.");

        verify(mockLlmClient, times(TOTAL_REQUESTS)).generateReport(anyString());
        verify(mockReportRepository, times(TOTAL_REQUESTS)).save(any(ObservationReport.class));
    }
}