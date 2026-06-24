package com.thomasmore.blc.labflow.entity.microbiology;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "staal_code_counter")
public class StaalCodeCounter {
    @Id
    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "last_number", nullable = false)
    private Long lastNumber;

    public StaalCodeCounter() {
    }

    public StaalCodeCounter(Integer year, Long lastNumber) {
        this.year = year;
        this.lastNumber = lastNumber;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Long getLastNumber() {
        return lastNumber;
    }

    public void setLastNumber(Long lastNumber) {
        this.lastNumber = lastNumber;
    }
}

