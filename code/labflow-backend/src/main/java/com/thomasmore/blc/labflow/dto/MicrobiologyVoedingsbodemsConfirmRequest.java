package com.thomasmore.blc.labflow.dto;

import java.util.List;

/**
 * Request body for POST /api/microbiology/staal/{id}/voedingsbodems/confirm.
 */
public class MicrobiologyVoedingsbodemsConfirmRequest {

    private List<Long> voedingsbodemIds;

    public List<Long> getVoedingsbodemIds() {
        return voedingsbodemIds;
    }

    public void setVoedingsbodemIds(List<Long> voedingsbodemIds) {
        this.voedingsbodemIds = voedingsbodemIds;
    }
}
