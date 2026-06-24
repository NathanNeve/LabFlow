package com.thomasmore.blc.labflow.dto.microbiology;

public class MicrobiologyStaalTestDto {
    private Long id;
    private Long testId;
    private String testCode;
    private String testNaam;
    private String waarde;
    private String commentaar;
    private boolean failed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    public String getTestNaam() {
        return testNaam;
    }

    public void setTestNaam(String testNaam) {
        this.testNaam = testNaam;
    }

    public String getWaarde() {
        return waarde;
    }

    public void setWaarde(String waarde) {
        this.waarde = waarde;
    }

    public String getCommentaar() {
        return commentaar;
    }

    public void setCommentaar(String commentaar) {
        this.commentaar = commentaar;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }
}
