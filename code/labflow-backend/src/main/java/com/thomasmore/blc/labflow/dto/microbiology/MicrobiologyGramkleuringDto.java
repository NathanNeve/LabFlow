package com.thomasmore.blc.labflow.dto.microbiology;

import java.util.List;

public class MicrobiologyGramkleuringDto {
    private Long staalTestId;
    private String commentaar;
    private List<MicrobiologyGramkleuringRowDto> rows;

    public Long getStaalTestId() {
        return staalTestId;
    }

    public void setStaalTestId(Long staalTestId) {
        this.staalTestId = staalTestId;
    }

    public String getCommentaar() {
        return commentaar;
    }

    public void setCommentaar(String commentaar) {
        this.commentaar = commentaar;
    }

    public List<MicrobiologyGramkleuringRowDto> getRows() {
        return rows;
    }

    public void setRows(List<MicrobiologyGramkleuringRowDto> rows) {
        this.rows = rows;
    }
}
