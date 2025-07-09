/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google.places;

public class PlacesAutocompleteRequest {
    private String input;
    private Geometry locationBias;
    private String[] includedRegionCodes;
    private String languageCode;
    private LatLng origin;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public Geometry getLocationBias() {
        return locationBias;
    }

    public void setLocationBias(Geometry locationBias) {
        this.locationBias = locationBias;
    }

    public String[] getIncludedRegionCodes() {
        return includedRegionCodes;
    }

    public void setIncludedRegionCodes(String[] includedRegionCodes) {
        this.includedRegionCodes = includedRegionCodes;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public LatLng getOrigin() {
        return origin;
    }

    public void setOrigin(LatLng origin) {
        this.origin = origin;
    }
}