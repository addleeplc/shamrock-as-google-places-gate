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
public class PlaceDetailsResponse {
    @JsonProperty("status")
    private GoogleApiStatus status;

    @JsonProperty("result")
    private PlaceDetailsResult result;

    @JsonProperty("html_attributions")
    private List htmlAttributions;

    public GoogleApiStatus getStatus() {
        return status;
    }

    public void setStatus(GoogleApiStatus status) {
        this.status = status;
    }

    public PlaceDetailsResult getResult() {
        return result;
    }

    public void setResult(PlaceDetailsResult results) {
        this.result = results;
    }

    public List getHtmlAttributions() {
        return htmlAttributions;
    }

    public void setHtmlAttributions(List htmlAttributions) {
        this.htmlAttributions = htmlAttributions;
    }

}

