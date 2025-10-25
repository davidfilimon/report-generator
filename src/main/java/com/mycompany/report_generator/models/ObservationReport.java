
package com.mycompany.report_generator.models;

import java.util.List;
import java.util.Map;

public class ObservationReport {

    private final String patientName;
    private final String doctorName;
    private final String observationSummary;
    private final String potentialDiagnosis;
    private final Map<String, String> vitalSigns;
    private final List<String> riskFactors;

    private ObservationReport(ObservationReportBuilder builder) {
        this.patientName = builder.patientName;
        this.doctorName = builder.doctorName;
        this.observationSummary = builder.observationSummary;
        this.potentialDiagnosis = builder.potentialDiagnosis;
        this.vitalSigns = builder.vitalSigns;
        this.riskFactors = builder.riskFactors;
    }

    public String getPatientName() { return patientName; }
    public String getDoctorName() { return doctorName; }
    public String getObservationSummary() { return observationSummary; }
    public String getPotentialDiagnosis() { return potentialDiagnosis; }
    public Map<String, String> getVitalSigns() { return vitalSigns; }
    public List<String> getRiskFactors() { return riskFactors; }

    public static ObservationReportBuilder builder() {
        return new ObservationReportBuilder();
    }

    public static class ObservationReportBuilder {
        private String patientName;
        private String doctorName;
        private String observationSummary;
        private String potentialDiagnosis;
        private Map<String, String> vitalSigns;
        private List<String> riskFactors;

        private ObservationReportBuilder() {}

        public ObservationReportBuilder patientName(String patientName) {
            this.patientName = patientName;
            return this;
        }
        public ObservationReportBuilder doctorName(String doctorName) {
            this.doctorName = doctorName;
            return this;
        }
        public ObservationReportBuilder observationSummary(String observationSummary) {
            this.observationSummary = observationSummary;
            return this;
        }
        public ObservationReportBuilder potentialDiagnosis(String potentialDiagnosis) {
            this.potentialDiagnosis = potentialDiagnosis;
            return this;
        }
        public ObservationReportBuilder vitalSigns(Map<String, String> vitalSigns) {
            this.vitalSigns = vitalSigns;
            return this;
        }
        public ObservationReportBuilder riskFactors(List<String> riskFactors) {
            this.riskFactors = riskFactors;
            return this;
        }

        public ObservationReport build() {
            return new ObservationReport(this);
        }
    }
}