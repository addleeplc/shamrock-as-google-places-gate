/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.services.dto.google.places;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.haulmont.shamrock.as.google.gate.dto.Place;
import com.haulmont.shamrock.as.google.gate.services.dto.google.ResponseStatus;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlacesResponse {
    @JsonProperty("status")
    private ResponseStatus status;

    @JsonProperty("results")
    private List<Place> results;

    @JsonProperty("next_page_token")
    private String nextPageToken;

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public List<Place> getResults() {
        return results;
    }

    public void setResults(List<Place> results) {
        this.results = results;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }
}

