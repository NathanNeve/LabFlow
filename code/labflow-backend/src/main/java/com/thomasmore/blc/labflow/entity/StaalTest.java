package com.thomasmore.blc.labflow.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "staal_test")
public class StaalTest implements Serializable {
    @EmbeddedId
    private StaalTestId id = new StaalTestId();

    @ManyToOne
    @MapsId("staalId")
    @JoinColumn(name = "staal_id")
    @JsonBackReference
    private Staal staal;

    @ManyToOne
    @MapsId("testId")
    @JoinColumn(name = "test_id")
    private Test test; // Remove @JsonManagedReference here

    @Nullable
    private String result;

    public StaalTest() {
    }

    public StaalTest(Staal staal, Test test) {
        this.staal = staal;
        this.test = test;
        this.id = new StaalTestId(staal.getId(), test.getId());
    }

    public StaalTestId getId() {
        return id;
    }

    public void setId(StaalTestId id) {
        this.id = id;
    }

    public Staal getStaal() {
        return staal;
    }

    public void setStaal(Staal staal) {
        this.staal = staal;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }

    @Nullable
    public String getResult() {
        return result;
    }

    public void setResult(@Nullable String result) {
        this.result = result;
    }
}
