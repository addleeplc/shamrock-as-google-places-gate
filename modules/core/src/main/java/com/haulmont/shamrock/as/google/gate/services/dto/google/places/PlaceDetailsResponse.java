/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.services.dto.google.places;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.haulmont.shamrock.as.google.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.gate.services.dto.google.ResponseStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaceDetailsResponse {
    @JsonProperty("status")
    private ResponseStatus status;

    @JsonProperty("result")
    private PlaceDetails result;

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public PlaceDetails getResult() {
        return result;
    }

    public void setResult(PlaceDetails results) {
        this.result = results;
    }

}

