package com.thomasmore.blc.labflow.dto.microbiology;

import java.util.List;

public class MicrobiologyVoedingsbodemNotebookDto {
    private Long linkId;
    private Long voedingsbodemId;
    private String voedingsbodemNaam;
    private String commentaar;
    private List<MicrobiologyVoedingsbodemLogEntry> logs;

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public Long getVoedingsbodemId() {
        return voedingsbodemId;
    }

    public void setVoedingsbodemId(Long voedingsbodemId) {
        this.voedingsbodemId = voedingsbodemId;
    }

    public String getVoedingsbodemNaam() {
        return voedingsbodemNaam;
    }

    public void setVoedingsbodemNaam(String voedingsbodemNaam) {
        this.voedingsbodemNaam = voedingsbodemNaam;
    }

    public String getCommentaar() {
        return commentaar;
    }

    public void setCommentaar(String commentaar) {
        this.commentaar = commentaar;
    }

    public List<MicrobiologyVoedingsbodemLogEntry> getLogs() {
        return logs;
    }

    public void setLogs(List<MicrobiologyVoedingsbodemLogEntry> logs) {
        this.logs = logs;
    }
}
