package com.thomasmore.blc.labflow.entity.microbiology;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class StaalTestVoedingsbodem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "staal_test_id", nullable = false)
    private StaalTest staalTest;

    @ManyToOne(optional = false)
    @JoinColumn(name = "voedingsbodem_id", nullable = false)
    private Voedingsbodem voedingsbodem;

    private String commentaar;

    public StaalTestVoedingsbodem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StaalTest getStaalTest() {
        return staalTest;
    }

    public void setStaalTest(StaalTest staalTest) {
        this.staalTest = staalTest;
    }

    public Voedingsbodem getVoedingsbodem() {
        return voedingsbodem;
    }

    public void setVoedingsbodem(Voedingsbodem voedingsbodem) {
        this.voedingsbodem = voedingsbodem;
    }

    public String getCommentaar() {
        return commentaar;
    }

    public void setCommentaar(String commentaar) {
        this.commentaar = commentaar;
    }
}
