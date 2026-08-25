package com.thomasmore.blc.labflow.dto.microbiology;

import com.thomasmore.blc.labflow.entity.microbiology.StaalType;
import com.thomasmore.blc.labflow.entity.microbiology.Voedingsbodem;

import java.util.List;

public class MicrobiologyCatalogTestResponse {
    private Long id;
    private String testCode;
    private String naam;
    private boolean extraTest;
    private String testType;
    private StaalType staalType;
    private List<Voedingsbodem> voedingsbodems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public boolean isExtraTest() {
        return extraTest;
    }

    public void setExtraTest(boolean extraTest) {
        this.extraTest = extraTest;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public StaalType getStaalType() {
        return staalType;
    }

    public void setStaalType(StaalType staalType) {
        this.staalType = staalType;
    }

    public List<Voedingsbodem> getVoedingsbodems() {
        return voedingsbodems;
    }

    public void setVoedingsbodems(List<Voedingsbodem> voedingsbodems) {
        this.voedingsbodems = voedingsbodems;
    }
}
