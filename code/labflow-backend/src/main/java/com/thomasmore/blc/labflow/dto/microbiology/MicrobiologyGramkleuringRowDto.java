package com.thomasmore.blc.labflow.dto.microbiology;

public class MicrobiologyGramkleuringRowDto {
    private String bepaling;
    private String score;
    private String commentaar;

    public MicrobiologyGramkleuringRowDto() {
    }

    public MicrobiologyGramkleuringRowDto(String bepaling, String score, String commentaar) {
        this.bepaling = bepaling;
        this.score = score;
        this.commentaar = commentaar;
    }

    public String getBepaling() {
        return bepaling;
    }

    public void setBepaling(String bepaling) {
        this.bepaling = bepaling;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getCommentaar() {
        return commentaar;
    }

    public void setCommentaar(String commentaar) {
        this.commentaar = commentaar;
    }
}
