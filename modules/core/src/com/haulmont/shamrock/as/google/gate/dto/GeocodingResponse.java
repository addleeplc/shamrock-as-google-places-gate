/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodingResponse {
    @JsonProperty("status")
    private GoogleApiStatus status;

    @JsonProperty("results")
    private List<GeocodingResult> results;

    public GoogleApiStatus getStatus() {
        return status;
    }

    public void setStatus(GoogleApiStatus status) {
        this.status = status;
    }

    public List<GeocodingResult> getResults() {
        return results;
    }

    public void setResults(List<GeocodingResult> results) {
        this.results = results;
    }

}
