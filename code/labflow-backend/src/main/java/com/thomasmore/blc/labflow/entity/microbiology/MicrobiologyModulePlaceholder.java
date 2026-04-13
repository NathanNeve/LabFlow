package com.thomasmore.blc.labflow.entity.microbiology;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Placeholder so the microbiology persistence unit has a mapped entity.
 * Replace or extend when this module gets real tables.
 */
@Entity
public class MicrobiologyModulePlaceholder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label = "reserved";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
