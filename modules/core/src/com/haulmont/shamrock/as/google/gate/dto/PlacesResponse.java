/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.dto;

import java.util.List;

public class PlacesResponse {
    private String status;
    private List<PlacesResult> results;

    public PlacesResponse() {}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PlacesResult> getResults() {
        return results;
    }

    public void setResults(List<PlacesResult> results) {
        this.results = results;
    }

}

