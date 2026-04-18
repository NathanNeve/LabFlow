package com.thomasmore.blc.labflow.entity.microbiology;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Antibiogram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "antibiotica_id", nullable = false)
    private Antibiotica antibiotica;

    @ManyToOne(optional = false)
    @JoinColumn(name = "staal_id", nullable = false)
    private Staal staal;

    @Enumerated(EnumType.STRING)
    private Bepaling bepaling;

    public Antibiogram() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Antibiotica getAntibiotica() {
        return antibiotica;
    }

    public void setAntibiotica(Antibiotica antibiotica) {
        this.antibiotica = antibiotica;
    }

    public Staal getStaal() {
        return staal;
    }

    public void setStaal(Staal staal) {
        this.staal = staal;
    }

    public Bepaling getBepaling() {
        return bepaling;
    }

    public void setBepaling(Bepaling bepaling) {
        this.bepaling = bepaling;
    }
}
