/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.services.dto.google.geocoding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.haulmont.shamrock.as.google.gate.services.dto.google.ResponseStatus;
import com.haulmont.shamrock.as.google.gate.dto.PlaceDetails;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodingResponse {
    @JsonProperty("status")
    private ResponseStatus status;

    @JsonProperty("results")
    private List<PlaceDetails> results;

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public List<PlaceDetails> getResults() {
        return results;
    }

    public void setResults(List<PlaceDetails> results) {
        this.results = results;
    }

}
