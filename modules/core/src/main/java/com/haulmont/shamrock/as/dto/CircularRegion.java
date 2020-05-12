/*
 * Copyright 2008 - 2020 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CircularRegion extends LatLon {

    @JsonProperty("radius")
    private Double radius;

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }
}
