package com.thomasmore.blc.labflow.entity.hematology;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // voor auto-increment in SQLite
    private Long id;

    @Column(unique = true)
    private String testCode;

    private String naam;

    // foreign key naar de staal tabel
    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<StaalTest> stalen = new ArrayList<>();

    // foreign key naar de eenheid tabel
    @ManyToOne
    @JoinColumn(name = "eenheid_id", nullable = false)
    private Eenheid eenheid;

    // foreign key naar de testcategorie tabel
    @ManyToOne
    @JoinColumn(name = "testcategorie_id", nullable = false)
    private Testcategorie testcategorie;

    @Nullable
    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL)
    /** Lazy collection; never serialize in JSON (session closed before MVC writes the body). */
    @JsonIgnore
    private Set<Referentiewaarde> referentiewaardes;

    // lege constructor
    public Test() {
    }

    // constructor met argumenten
    public Test(String testCode, String naam, Eenheid eenheid, Testcategorie testcategorie) {
        this.testCode = testCode;
        this.naam = naam;
        this.eenheid = eenheid;
        this.testcategorie = testcategorie;
    }

    // getters en setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public List<StaalTest> getStalen() {
        return stalen;
    }

    public void setStalen(List<StaalTest> stalen) {
        this.stalen = stalen;
    }

    public Eenheid getEenheid() {
        return eenheid;
    }

    public void setEenheid(Eenheid eenheid) {
        this.eenheid = eenheid;
    }

    public Testcategorie getTestcategorie() {
        return testcategorie;
    }

    public void setTestcategorie(Testcategorie testcategorie) {
        this.testcategorie = testcategorie;
    }

    public Set<Referentiewaarde> getReferentiewaardes() {
        return referentiewaardes;
    }

    public void setReferentiewaardes(Set<Referentiewaarde> referentiewaardes) {
        this.referentiewaardes = referentiewaardes;
    }
}
