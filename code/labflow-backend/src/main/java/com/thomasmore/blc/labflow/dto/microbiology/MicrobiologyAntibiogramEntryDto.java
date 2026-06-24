package com.thomasmore.blc.labflow.dto.microbiology;

public class MicrobiologyAntibiogramEntryDto {
    private Long antibioticaId;
    private String antibioticaNaam;
    private String beoordeling;

    public Long getAntibioticaId() {
        return antibioticaId;
    }

    public void setAntibioticaId(Long antibioticaId) {
        this.antibioticaId = antibioticaId;
    }

    public String getAntibioticaNaam() {
        return antibioticaNaam;
    }

    public void setAntibioticaNaam(String antibioticaNaam) {
        this.antibioticaNaam = antibioticaNaam;
    }

    public String getBeoordeling() {
        return beoordeling;
    }

    public void setBeoordeling(String beoordeling) {
        this.beoordeling = beoordeling;
    }
}
