/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google.places;

import java.util.List;

public class SearchNearbyRequest {
    private String languageCode;
    private String regionCode;
    private List<String> includedTypes;
    private List<String> excludedTypes;
    private List<String> includedPrimaryTypes;
    private List<String> excludedPrimaryTypes;
    private Integer maxResultCount;
    private LocationRestriction locationRestriction;
    private RankPreference rankPreference;
    private RoutingParameters routingParameters;

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

    public List<String> getIncludedTypes() {
        return includedTypes;
    }

    public void setIncludedTypes(List<String> includedTypes) {
        this.includedTypes = includedTypes;
    }

    public List<String> getExcludedTypes() {
        return excludedTypes;
    }

    public void setExcludedTypes(List<String> excludedTypes) {
        this.excludedTypes = excludedTypes;
    }

    public List<String> getIncludedPrimaryTypes() {
        return includedPrimaryTypes;
    }

    public void setIncludedPrimaryTypes(List<String> includedPrimaryTypes) {
        this.includedPrimaryTypes = includedPrimaryTypes;
    }

    public List<String> getExcludedPrimaryTypes() {
        return excludedPrimaryTypes;
    }

    public void setExcludedPrimaryTypes(List<String> excludedPrimaryTypes) {
        this.excludedPrimaryTypes = excludedPrimaryTypes;
    }

    public Integer getMaxResultCount() {
        return maxResultCount;
    }

    public void setMaxResultCount(Integer maxResultCount) {
        this.maxResultCount = maxResultCount;
    }

    public LocationRestriction getLocationRestriction() {
        return locationRestriction;
    }

    public void setLocationRestriction(LocationRestriction locationRestriction) {
        this.locationRestriction = locationRestriction;
    }

    public RankPreference getRankPreference() {
        return rankPreference;
    }

    public void setRankPreference(RankPreference rankPreference) {
        this.rankPreference = rankPreference;
    }

    public RoutingParameters getRoutingParameters() {
        return routingParameters;
    }

    public void setRoutingParameters(RoutingParameters routingParameters) {
        this.routingParameters = routingParameters;
    }
}