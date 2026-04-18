package com.thomasmore.blc.labflow.entity.hematology;

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
    @JoinColumn(name = "staal_id", nullable = false)
    @JsonBackReference
    private Staal staal;

    @ManyToOne
    @MapsId("testId")
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Nullable
    private String result;

    @Nullable
    private String note;

    private Boolean failed = false;

    public StaalTest() {
    }

    public StaalTest(Staal staal, Test test) {
        this.staal = staal;
        this.test = test;
        this.id = new StaalTestId(staal.getId(), test.getId());
    }

    // Getters & setters
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
        if (staal != null) {
            this.id.setStaalId(staal.getId());
        }
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
        if (test != null) {
            this.id.setTestId(test.getId());
        }
    }

    @Nullable
    public String getResult() {
        return result;
    }

    public void setResult(@Nullable String result) {
        this.result = result;
    }

    @Nullable
    public String getNote() {
        return note;
    }

    public void setNote(@Nullable String note) {
        this.note = note;
    }

    public Boolean getFailed() {
        return failed;
    }

    public void setFailed(Boolean failed) {
        this.failed = failed;
    }
}
