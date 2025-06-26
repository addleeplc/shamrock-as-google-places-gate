/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google.places;

public class RouteModifiers {
    private boolean avoidTolls;
    private boolean avoidHighways;
    private boolean avoidFerries;
    private boolean avoidIndoor;

    public boolean isAvoidTolls() {
        return avoidTolls;
    }

    public void setAvoidTolls(boolean avoidTolls) {
        this.avoidTolls = avoidTolls;
    }

    public boolean isAvoidHighways() {
        return avoidHighways;
    }

    public void setAvoidHighways(boolean avoidHighways) {
        this.avoidHighways = avoidHighways;
    }

    public boolean isAvoidFerries() {
        return avoidFerries;
    }

    public void setAvoidFerries(boolean avoidFerries) {
        this.avoidFerries = avoidFerries;
    }

    public boolean isAvoidIndoor() {
        return avoidIndoor;
    }

    public void setAvoidIndoor(boolean avoidIndoor) {
        this.avoidIndoor = avoidIndoor;
    }
}