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
public class PlacesResponse {
    @JsonProperty("status")
    private GoogleApiStatus status;

    @JsonProperty("results")
    private List<PlacesResult> results;

    @JsonProperty("next_page_token")
    private String nextPageToken;

    public GoogleApiStatus getStatus() {
        return status;
    }

    public void setStatus(GoogleApiStatus status) {
        this.status = status;
    }

    public List<PlacesResult> getResults() {
        return results;
    }

    public void setResults(List<PlacesResult> results) {
        this.results = results;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }
}

