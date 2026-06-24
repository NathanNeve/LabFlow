package com.thomasmore.blc.labflow.dto;

import java.util.List;

/**
 * Response DTO for GET /api/microbiology/tests.
 * Includes the linked voedingsbodems for the test.
 */
public class MicrobiologyTestResponse {
    private Long id;
    private String testCode;
    private String naam;
    private boolean extraTest;
    private String testType;
    private List<String> voedingsbodems;

    public MicrobiologyTestResponse() {
    }

    public MicrobiologyTestResponse(Long id, String testCode, String naam, boolean extraTest, String testType,
                                    List<String> voedingsbodems) {
        this.id = id;
        this.testCode = testCode;
        this.naam = naam;
        this.extraTest = extraTest;
        this.testType = testType;
        this.voedingsbodems = voedingsbodems;
    }

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

    public List<String> getVoedingsbodems() {
        return voedingsbodems;
    }

    public void setVoedingsbodems(List<String> voedingsbodems) {
        this.voedingsbodems = voedingsbodems;
    }
}
