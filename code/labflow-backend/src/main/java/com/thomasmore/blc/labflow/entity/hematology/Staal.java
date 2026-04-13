package com.thomasmore.blc.labflow.entity.hematology;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.*;

@Entity
public class Staal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // voor auto-increment in SQLite
    private Long id;

    @Column(unique = true)
    private Long staalCode;

    private String patientVoornaam;

    private String patientAchternaam;

    private LocalDate patientGeboorteDatum;

    private char patientGeslacht;

    private String laborantNaam;

    private String laborantRnummer;

    private Date aanmaakDatum;

    /** Auth user id (stored in hematology DB; no cross-database FK). */
    @JsonIgnore
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "staal", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JsonManagedReference
    private List<StaalTest> registeredTests = new ArrayList<>();

    // enumerable status
    public enum Status {
        AANGEMAAKT, GEREGISTREERD, KLAAR
    }

    @Enumerated(EnumType.STRING) // Opslaan enumerable als een string
    private Status status;

    // lege constructor
    public Staal() {
        this.aanmaakDatum = new Date();
    }

    // constructor voor het registreren van een staal zonder tests
    public Staal(Long staalCode, String patientVoornaam, String patientAchternaam, LocalDate patientGeboorteDatum,
                 char patientGeslacht, String laborantNaam, String laborantRnummer, Long userId) {
        this.staalCode = staalCode;
        this.patientVoornaam = patientVoornaam;
        this.patientAchternaam = patientAchternaam;
        this.patientGeboorteDatum = patientGeboorteDatum;
        this.patientGeslacht = patientGeslacht;
        this.laborantNaam = laborantNaam;
        this.laborantRnummer = laborantRnummer;
        this.userId = userId;
        this.aanmaakDatum = new Date();
        this.status = Status.AANGEMAAKT;
    }

    // constructor voor het registreren van een staal met tests
    public Staal(Long staalCode, String patientVoornaam, String patientAchternaam, LocalDate patientGeboorteDatum,
                 char patientGeslacht, String laborantNaam, String laborantRnummer, Long userId,
                 List<StaalTest> registeredTests) {
        this.staalCode = staalCode;
        this.patientVoornaam = patientVoornaam;
        this.patientAchternaam = patientAchternaam;
        this.patientGeboorteDatum = patientGeboorteDatum;
        this.patientGeslacht = patientGeslacht;
        this.laborantNaam = laborantNaam;
        this.laborantRnummer = laborantRnummer;
        this.userId = userId;
        this.setRegisteredTests(registeredTests); // Use setter to ensure proper association
        this.aanmaakDatum = new Date();
        this.status = Status.AANGEMAAKT;
    }

    @JsonGetter("user")
    public Map<String, Long> getUserRefForJson() {
        return userId == null ? null : Collections.singletonMap("id", userId);
    }

    @JsonSetter("user")
    public void setUserFromJson(Map<String, Object> user) {
        if (user == null || user.get("id") == null) {
            return;
        }
        Object idObj = user.get("id");
        if (idObj instanceof Number n) {
            this.userId = n.longValue();
        } else if (idObj instanceof String s && !s.isBlank()) {
            this.userId = Long.parseLong(s);
        }
    }

    // getters en setters
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

    public LocalDate getPatientGeboorteDatum() {
        return patientGeboorteDatum;
    }

    public void setPatientGeboorteDatum(LocalDate patientGeboorteDatum) {
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

    public Date getAanmaakDatum() {
        return aanmaakDatum;
    }

    public void setAanmaakDatum(Date aanmaakDatum) {
        this.aanmaakDatum = aanmaakDatum;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<StaalTest> getRegisteredTests() {
        return registeredTests;
    }

    public void setRegisteredTests(List<StaalTest> newTests) {
        // Verwijderen bestaande tests
        registeredTests.removeIf(existingTest -> !newTests.contains(existingTest));

        // Nieuwe tests toevoegen
        for (StaalTest newTest : newTests) {
            if (!registeredTests.contains(newTest)) {
                registeredTests.add(newTest);
                newTest.setStaal(this);
            }
        }
    }

    // methodes voor het toevoegen en verwijderen van tests gekoppeld aan één staal
    public void addRegisteredTest(StaalTest test) {
        registeredTests.add(test);
        test.setStaal(this);
    }

    public void removeRegisteredTest(StaalTest test) {
        registeredTests.remove(test);
        test.setStaal(null);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
