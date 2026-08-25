package com.thomasmore.blc.labflow.dto.microbiology;

import java.util.List;

public class MicrobiologyCatalogTestRequest {
    private String testCode;
    private String naam;
    private boolean extraTest;
    private String testType;
    private Long staalTypeId;
    private List<Long> voedingsbodemIds;

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

    public Long getStaalTypeId() {
        return staalTypeId;
    }

    public void setStaalTypeId(Long staalTypeId) {
        this.staalTypeId = staalTypeId;
    }

    public List<Long> getVoedingsbodemIds() {
        return voedingsbodemIds;
    }

    public void setVoedingsbodemIds(List<Long> voedingsbodemIds) {
        this.voedingsbodemIds = voedingsbodemIds;
    }
}
