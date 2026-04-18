package com.thomasmore.blc.labflow.entity.microbiology;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "test_voedingsbodem")
public class TestVoedingsbodem implements Serializable {

    @EmbeddedId
    private TestVoedingsbodemId id = new TestVoedingsbodemId();

    @ManyToOne(optional = false)
    @MapsId("testId")
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @ManyToOne(optional = false)
    @MapsId("voedingsbodemId")
    @JoinColumn(name = "voedingsbodem_id", nullable = false)
    private Voedingsbodem voedingsbodem;

    public TestVoedingsbodem() {
    }

    public TestVoedingsbodem(Test test, Voedingsbodem voedingsbodem) {
        this.test = test;
        this.voedingsbodem = voedingsbodem;
        this.id = new TestVoedingsbodemId(test.getId(), voedingsbodem.getId());
    }

    public TestVoedingsbodemId getId() {
        return id;
    }

    public void setId(TestVoedingsbodemId id) {
        this.id = id;
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

    public Voedingsbodem getVoedingsbodem() {
        return voedingsbodem;
    }

    public void setVoedingsbodem(Voedingsbodem voedingsbodem) {
        this.voedingsbodem = voedingsbodem;
        if (voedingsbodem != null) {
            this.id.setVoedingsbodemId(voedingsbodem.getId());
        }
    }
}
