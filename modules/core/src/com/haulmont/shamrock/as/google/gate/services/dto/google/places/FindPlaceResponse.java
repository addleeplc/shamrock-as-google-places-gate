/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.services.dto.google.places;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haulmont.shamrock.as.google.gate.services.dto.google.ResponseStatus;
import com.haulmont.shamrock.as.google.gate.dto.Place;

import java.util.List;

public class FindPlaceResponse {
    @JsonProperty("status")
    private ResponseStatus status;

    @JsonProperty("candidates")
    private List<Place> candidates;

    public List<Place> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Place> candidates) {
        this.candidates = candidates;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }
}
