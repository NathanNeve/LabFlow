package com.thomasmore.blc.labflow.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request body for POST /api/microbiology/staal/{id}/tests.
 */
public class MicrobiologyStaalTestsRequest {

    private String patientVoornaam;
    private String patientAchternaam;
    private LocalDateTime patientGeboorteDatum;
    /** Single character e.g. M, V, X */
    private String patientGeslacht;
    private List<Long> testIds;

    public String getPatientVoornaam() {
        return patientVoornaam;
    }

    public void setPatientVoornaam(String patientVoornaam) {
        this.patientVoornaam = patientVoornaam;
    }

    public String getPatientAchternaam() {
        return patientAchternaam;
    }

    public void setPatientAchternaam(String patientAchternaam) {
        this.patientAchternaam = patientAchternaam;
    }

    public LocalDateTime getPatientGeboorteDatum() {
        return patientGeboorteDatum;
    }

    public void setPatientGeboorteDatum(LocalDateTime patientGeboorteDatum) {
        this.patientGeboorteDatum = patientGeboorteDatum;
    }

    public String getPatientGeslacht() {
        return patientGeslacht;
    }

    public void setPatientGeslacht(String patientGeslacht) {
        this.patientGeslacht = patientGeslacht;
    }

    public List<Long> getTestIds() {
        return testIds;
    }

    public void setTestIds(List<Long> testIds) {
        this.testIds = testIds;
    }
}
