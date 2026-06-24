package com.thomasmore.blc.labflow.dto.microbiology;

import java.util.List;

public class MicrobiologyAntibiogramUpdateRequest {
    private List<MicrobiologyAntibiogramEntryUpdate> entries;

    public List<MicrobiologyAntibiogramEntryUpdate> getEntries() {
        return entries;
    }

    public void setEntries(List<MicrobiologyAntibiogramEntryUpdate> entries) {
        this.entries = entries;
    }

    public static class MicrobiologyAntibiogramEntryUpdate {
        private Long antibioticaId;
        private String beoordeling;

        public Long getAntibioticaId() {
            return antibioticaId;
        }

        public void setAntibioticaId(Long antibioticaId) {
            this.antibioticaId = antibioticaId;
        }

        public String getBeoordeling() {
            return beoordeling;
        }

        public void setBeoordeling(String beoordeling) {
            this.beoordeling = beoordeling;
        }
    }
}
