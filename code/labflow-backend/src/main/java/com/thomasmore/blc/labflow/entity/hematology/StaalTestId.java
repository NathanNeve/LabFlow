package com.thomasmore.blc.labflow.entity.hematology;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StaalTestId implements Serializable {
    private Long staalId;
    private Long testId;

    public StaalTestId() {
    }

    public StaalTestId(Long staalId, Long testId) {
        this.staalId = staalId;
        this.testId = testId;
    }

    public Long getStaalId() {
        return staalId;
    }

    public void setStaalId(Long staalId) {
        this.staalId = staalId;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaalTestId that = (StaalTestId) o;
        return Objects.equals(staalId, that.staalId) && Objects.equals(testId, that.testId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(staalId, testId);
    }
}
