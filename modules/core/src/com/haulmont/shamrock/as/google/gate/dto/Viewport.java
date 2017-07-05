/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.dto;

class Viewport {
    private Coordinates southwest;
    private Coordinates northeast;

    Viewport() {
    }

    public Coordinates getSouthwest() {
        return southwest;
    }

    public void setSouthwest(Coordinates southwest) {
        this.southwest = southwest;
    }

    public Coordinates getNortheast() {
        return northeast;
    }

    public void setNortheast(Coordinates northeast) {
        this.northeast = northeast;
    }
}

