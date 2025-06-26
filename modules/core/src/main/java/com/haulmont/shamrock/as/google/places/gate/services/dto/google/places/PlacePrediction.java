/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google.places;

import java.util.List;

public class PlacePrediction {
    private String place;
    private String placeId;
    private FormattableText text;
    private StructuredFormat structuredFormat;
    private List<String> types;
    private Integer distanceMeters;

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public FormattableText getText() {
        return text;
    }

    public void setText(FormattableText text) {
        this.text = text;
    }

    public StructuredFormat getStructuredFormat() {
        return structuredFormat;
    }

    public void setStructuredFormat(StructuredFormat structuredFormat) {
        this.structuredFormat = structuredFormat;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public Integer getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Integer distanceMeters) {
        this.distanceMeters = distanceMeters;
    }
}