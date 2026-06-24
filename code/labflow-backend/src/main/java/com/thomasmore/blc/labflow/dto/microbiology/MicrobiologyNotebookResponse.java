package com.thomasmore.blc.labflow.dto.microbiology;

import java.util.List;

public class MicrobiologyNotebookResponse {
    private Long id;
    private Long staalCode;
    private String patientVoornaam;
    private String patientAchternaam;
    private String patientGeboorteDatum;
    private String patientGeslacht;
    private String commentaar;
    private boolean voltooidAlgemeneTesten;
    private boolean voltooidVoedingsbodems;
    private boolean voltooidGramkleuring;
    private boolean voltooidAntibiogram;
    private String status;
    private List<String> activeSections;
    private List<MicrobiologyStaalTestDto> algemeneTesten;
    private List<MicrobiologyVoedingsbodemNotebookDto> voedingsbodems;
    private MicrobiologyGramkleuringDto gramkleuring;
    private List<MicrobiologyAntibiogramEntryDto> antibiogram;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getPatientGeboorteDatum() {
        return patientGeboorteDatum;
    }

    public void setPatientGeboorteDatum(String patientGeboorteDatum) {
        this.patientGeboorteDatum = patientGeboorteDatum;
    }

    public String getPatientGeslacht() {
        return patientGeslacht;
    }

    public void setPatientGeslacht(String patientGeslacht) {
        this.patientGeslacht = patientGeslacht;
    }

    public String getCommentaar() {
        return commentaar;
    }

    public void setCommentaar(String commentaar) {
        this.commentaar = commentaar;
    }

    public boolean isVoltooidAlgemeneTesten() {
        return voltooidAlgemeneTesten;
    }

    public void setVoltooidAlgemeneTesten(boolean voltooidAlgemeneTesten) {
        this.voltooidAlgemeneTesten = voltooidAlgemeneTesten;
    }

    public boolean isVoltooidVoedingsbodems() {
        return voltooidVoedingsbodems;
    }

    public void setVoltooidVoedingsbodems(boolean voltooidVoedingsbodems) {
        this.voltooidVoedingsbodems = voltooidVoedingsbodems;
    }

    public boolean isVoltooidGramkleuring() {
        return voltooidGramkleuring;
    }

    public void setVoltooidGramkleuring(boolean voltooidGramkleuring) {
        this.voltooidGramkleuring = voltooidGramkleuring;
    }

    public boolean isVoltooidAntibiogram() {
        return voltooidAntibiogram;
    }

    public void setVoltooidAntibiogram(boolean voltooidAntibiogram) {
        this.voltooidAntibiogram = voltooidAntibiogram;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getActiveSections() {
        return activeSections;
    }

    public void setActiveSections(List<String> activeSections) {
        this.activeSections = activeSections;
    }

    public List<MicrobiologyStaalTestDto> getAlgemeneTesten() {
        return algemeneTesten;
    }

    public void setAlgemeneTesten(List<MicrobiologyStaalTestDto> algemeneTesten) {
        this.algemeneTesten = algemeneTesten;
    }

    public List<MicrobiologyVoedingsbodemNotebookDto> getVoedingsbodems() {
        return voedingsbodems;
    }

    public void setVoedingsbodems(List<MicrobiologyVoedingsbodemNotebookDto> voedingsbodems) {
        this.voedingsbodems = voedingsbodems;
    }

    public MicrobiologyGramkleuringDto getGramkleuring() {
        return gramkleuring;
    }

    public void setGramkleuring(MicrobiologyGramkleuringDto gramkleuring) {
        this.gramkleuring = gramkleuring;
    }

    public List<MicrobiologyAntibiogramEntryDto> getAntibiogram() {
        return antibiogram;
    }

    public void setAntibiogram(List<MicrobiologyAntibiogramEntryDto> antibiogram) {
        this.antibiogram = antibiogram;
    }
}
