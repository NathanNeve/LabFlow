package com.thomasmore.blc.labflow.entity.microbiology;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "staal_test_voedingsbodem_log")
public class StaalTestVoedingsbodemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String organisme;

    private String beoordeling;

    private String sts;

    private String commentaar;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "staal_test_voedingsbodem_id", nullable = false)
    private StaalTestVoedingsbodem staalTestVoedingsbodem;

    public StaalTestVoedingsbodemLog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrganisme() {
        return organisme;
    }

    public void setOrganisme(String organisme) {
        this.organisme = organisme;
    }

    public String getBeoordeling() {
        return beoordeling;
    }

    public void setBeoordeling(String beoordeling) {
        this.beoordeling = beoordeling;
    }

    public String getSts() {
        return sts;
    }

    public void setSts(String sts) {
        this.sts = sts;
    }

    public String getCommentaar() {
        return commentaar;
    }

    public void setCommentaar(String commentaar) {
        this.commentaar = commentaar;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public StaalTestVoedingsbodem getStaalTestVoedingsbodem() {
        return staalTestVoedingsbodem;
    }

    public void setStaalTestVoedingsbodem(StaalTestVoedingsbodem staalTestVoedingsbodem) {
        this.staalTestVoedingsbodem = staalTestVoedingsbodem;
    }
}
