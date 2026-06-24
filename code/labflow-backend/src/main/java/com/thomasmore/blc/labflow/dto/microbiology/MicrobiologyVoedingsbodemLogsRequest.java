package com.thomasmore.blc.labflow.dto.microbiology;

import java.util.List;

public class MicrobiologyVoedingsbodemLogsRequest {
    private List<MicrobiologyVoedingsbodemLogEntry> logs;

    public List<MicrobiologyVoedingsbodemLogEntry> getLogs() {
        return logs;
    }

    public void setLogs(List<MicrobiologyVoedingsbodemLogEntry> logs) {
        this.logs = logs;
    }
}
