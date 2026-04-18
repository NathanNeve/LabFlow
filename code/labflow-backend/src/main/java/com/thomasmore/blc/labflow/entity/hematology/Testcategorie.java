package com.thomasmore.blc.labflow.entity.hematology;

import jakarta.persistence.*;

@Entity
public class Testcategorie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // voor auto-increment in SQLite
    private Long id;

    @Column(unique=true)
    private String naam;

    private String kleur;

    private String kleurnaam;

    // lege constructor
    public Testcategorie() {
    }

    // constructor met arguments
    public Testcategorie(String naam, String kleur, String kleurnaam) {
        this.naam = naam;
        this.kleur = kleur;
        this.kleurnaam = kleurnaam;
    }

    // getters en setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getKleur() {
        return kleur;
    }

    public void setKleur(String kleur) {
        this.kleur = kleur;
    }

    public String getKleurnaam() {
        return kleurnaam;
    }

    public void setKleurnaam(String kleurnaam) {
        this.kleurnaam = kleurnaam;
    }
}
