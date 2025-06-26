/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google.places;

public class SearchTextRequest {
    private String textQuery;
    private String languageCode;
    private String regionCode;
    private RankPreference rankPreference;
    private String includedType;
    private Boolean openNow;
    private Double minRating;
    private Double maxResultCount;
    private Integer pageSize;
    private String pageToken;
    private Boolean strictTypeFiltering;
    private Geometry locationBias;
    private Boolean includePureServiceAreaBusinesses;

    public String getTextQuery() {
        return textQuery;
    }

    public void setTextQuery(String textQuery) {
        this.textQuery = textQuery;
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

    public RankPreference getRankPreference() {
        return rankPreference;
    }

    public void setRankPreference(RankPreference rankPreference) {
        this.rankPreference = rankPreference;
    }

    public String getIncludedType() {
        return includedType;
    }

    public void setIncludedType(String includedType) {
        this.includedType = includedType;
    }

    public Boolean getOpenNow() {
        return openNow;
    }

    public void setOpenNow(Boolean openNow) {
        this.openNow = openNow;
    }

    public Double getMinRating() {
        return minRating;
    }

    public void setMinRating(Double minRating) {
        this.minRating = minRating;
    }

    public Double getMaxResultCount() {
        return maxResultCount;
    }

    public void setMaxResultCount(Double maxResultCount) {
        this.maxResultCount = maxResultCount;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getPageToken() {
        return pageToken;
    }

    public void setPageToken(String pageToken) {
        this.pageToken = pageToken;
    }

    public Boolean getStrictTypeFiltering() {
        return strictTypeFiltering;
    }

    public void setStrictTypeFiltering(Boolean strictTypeFiltering) {
        this.strictTypeFiltering = strictTypeFiltering;
    }

    public Geometry getLocationBias() {
        return locationBias;
    }

    public void setLocationBias(Geometry locationBias) {
        this.locationBias = locationBias;
    }

    public Boolean getIncludePureServiceAreaBusinesses() {
        return includePureServiceAreaBusinesses;
    }

    public void setIncludePureServiceAreaBusinesses(Boolean includePureServiceAreaBusinesses) {
        this.includePureServiceAreaBusinesses = includePureServiceAreaBusinesses;
    }
}