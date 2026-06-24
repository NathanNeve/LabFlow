package com.thomasmore.blc.labflow.dto.microbiology;

public class MicrobiologyStaalTestUpdateRequest {
    private String waarde;
    private String commentaar;
    private Boolean failed;

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

    public Boolean getFailed() {
        return failed;
    }

    public void setFailed(Boolean failed) {
        this.failed = failed;
    }
}
