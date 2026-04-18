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
public class Staal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY);
    private Long id;

    
    
}
