/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google.places;

public class PlacesAutocompleteRequest {
    private String input;
    private Geometry locationBias;
    private Geometry locationRestriction;
    private String[] includedPrimaryTypes;
    private String[] includedRegionCodes;
    private String languageCode;
    private String regionCode;
    private LatLng origin;
    private Integer inputOffset;
    private Boolean includeQueryPredictions;
    private String sessionToken;
    private Boolean includePureServiceAreaBusinesses;

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

    public Geometry getLocationRestriction() {
        return locationRestriction;
    }

    public void setLocationRestriction(Geometry locationRestriction) {
        this.locationRestriction = locationRestriction;
    }

    public String[] getIncludedPrimaryTypes() {
        return includedPrimaryTypes;
    }

    public void setIncludedPrimaryTypes(String[] includedPrimaryTypes) {
        this.includedPrimaryTypes = includedPrimaryTypes;
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

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public LatLng getOrigin() {
        return origin;
    }

    public void setOrigin(LatLng origin) {
        this.origin = origin;
    }

    public Integer getInputOffset() {
        return inputOffset;
    }

    public void setInputOffset(Integer inputOffset) {
        this.inputOffset = inputOffset;
    }

    public Boolean getIncludeQueryPredictions() {
        return includeQueryPredictions;
    }

    public void setIncludeQueryPredictions(Boolean includeQueryPredictions) {
        this.includeQueryPredictions = includeQueryPredictions;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public Boolean getIncludePureServiceAreaBusinesses() {
        return includePureServiceAreaBusinesses;
    }

    public void setIncludePureServiceAreaBusinesses(Boolean includePureServiceAreaBusinesses) {
        this.includePureServiceAreaBusinesses = includePureServiceAreaBusinesses;
    }
}