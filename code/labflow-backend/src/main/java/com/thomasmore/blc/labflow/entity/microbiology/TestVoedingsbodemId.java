package com.thomasmore.blc.labflow.entity.microbiology;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TestVoedingsbodemId implements Serializable {
    private Long testId;
    private Long voedingsbodemId;

    public TestVoedingsbodemId() {
    }

    public TestVoedingsbodemId(Long testId, Long voedingsbodemId) {
        this.testId = testId;
        this.voedingsbodemId = voedingsbodemId;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public Long getVoedingsbodemId() {
        return voedingsbodemId;
    }

    public void setVoedingsbodemId(Long voedingsbodemId) {
        this.voedingsbodemId = voedingsbodemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestVoedingsbodemId that = (TestVoedingsbodemId) o;
        return Objects.equals(testId, that.testId) && Objects.equals(voedingsbodemId, that.voedingsbodemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testId, voedingsbodemId);
    }
}
