package com.thomasmore.blc.labflow.entity.hematology;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.springframework.lang.Nullable;

@Entity
public class Referentiewaarde {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // voor auto-increment in SQLite
    private Long id;

    private String waarde;

    // foreign key naar de test tabel
    @ManyToOne
    @JoinColumn(name = "test_id", nullable = true)
    @JsonBackReference
    @Nullable
    private Test test;

    // Lege constructor
    public Referentiewaarde() {
    }

    // constructor met argumenten
    public Referentiewaarde(String waarde, Test test) {
        this.waarde = waarde;
        this.test = test;
    }

    // getters & setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWaarde() {
        return waarde;
    }

    public void setWaarde(String referentieWaarde) {
        this.waarde = referentieWaarde;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }
}
