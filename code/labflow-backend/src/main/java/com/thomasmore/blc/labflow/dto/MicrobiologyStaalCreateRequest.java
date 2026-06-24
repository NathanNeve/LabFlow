package com.thomasmore.blc.labflow.dto;

/**
 * Request body for POST /api/microbiology/staal.
 */
public class MicrobiologyStaalCreateRequest {
    private String laborantNaam;
    private String laborantRnummer;
    private Long staalTypeId;

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

