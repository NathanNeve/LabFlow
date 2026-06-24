package com.thomasmore.blc.labflow.entity.microbiology;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Staal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long staalCode;

    private String patientVoornaam;

    private String patientAchternaam;

    private LocalDateTime patientGeboorteDatum;

    private char patientGeslacht;

    private String laborantNaam;

    private String laborantRnummer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "staal_type_id", nullable = false)
    private StaalType staalType;

    private String commentaar;

    private boolean voltooidAlgemeneTesten;

    private boolean voltooidVoedingsbodems;

    private boolean voltooidGramkleuring;

    private boolean voltooidAntibiogram;

    @Enumerated(EnumType.STRING)
    private StaalStatus status = StaalStatus.AANGEMAAKT;

    public Staal() {
    }

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

    public LocalDateTime getPatientGeboorteDatum() {
        return patientGeboorteDatum;
    }

    public void setPatientGeboorteDatum(LocalDateTime patientGeboorteDatum) {
        this.patientGeboorteDatum = patientGeboorteDatum;
    }

    public char getPatientGeslacht() {
        return patientGeslacht;
    }

    public void setPatientGeslacht(char patientGeslacht) {
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

    public StaalType getStaalType() {
        return staalType;
    }

    public void setStaalType(StaalType staalType) {
        this.staalType = staalType;
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

    public StaalStatus getStatus() {
        return status;
    }

    public void setStatus(StaalStatus status) {
        this.status = status;
    }
}
