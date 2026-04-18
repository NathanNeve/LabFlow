package com.thomasmore.blc.labflow.dto;

import java.time.LocalDateTime;

/**
 * Request body for PUT /api/microbiology/staal/{id}.
 */
public class MicrobiologyStaalUpdateRequest {

    private Long staalCode;
    private String patientVoornaam;
    private String patientAchternaam;
    private LocalDateTime patientGeboorteDatum;
    /** Single character e.g. M, V, X */
    private String patientGeslacht;
    private String laborantNaam;
    private String laborantRnummer;
    private Long staalTypeId;

    public Long getStaalCode() {
        return staalCode;
    }

    public void setStaalCode(Long staalCode) {
        this.staalCode = staalCode;
    }

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

    public String getLaborantNaam() {
        return laborantNaam;
    }

    public void setLaborantNaam(String laborantNaam) {
        this.laborantNaam = laborantNaam;
    }

    public String getLaborantRnummer() {
        return laborantRnummer;
    }

    public void setLaborantRnummer(String laborantRnummer) {
        this.laborantRnummer = laborantRnummer;
    }

    public Long getStaalTypeId() {
        return staalTypeId;
    }

    public void setStaalTypeId(Long staalTypeId) {
        this.staalTypeId = staalTypeId;
    }
}
