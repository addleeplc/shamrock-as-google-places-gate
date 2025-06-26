/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google.places;

public class Viewport {
    private LatLng low;
    private LatLng high;

    public Viewport() {
    }

    public Viewport(LatLng low, LatLng high) {
        this.low = low;
        this.high = high;
    }

    public LatLng getLow() {
        return low;
    }

    public void setLow(LatLng low) {
        this.low = low;
    }

    public LatLng getHigh() {
        return high;
    }

    public void setHigh(LatLng high) {
        this.high = high;
    }
}