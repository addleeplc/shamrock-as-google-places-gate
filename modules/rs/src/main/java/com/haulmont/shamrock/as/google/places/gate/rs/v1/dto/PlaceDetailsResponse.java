/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.rs.v1.dto;

import com.haulmont.monaco.response.Response;

public class PlaceDetailsResponse extends Response {
    private PlaceDetails place;

    public PlaceDetailsResponse() {
    }

    public PlaceDetailsResponse(PlaceDetails place) {
        this.place = place;
    }

    public PlaceDetails getPlace() {
        return place;
    }

    public void setPlace(PlaceDetails place) {
        this.place = place;
    }

}