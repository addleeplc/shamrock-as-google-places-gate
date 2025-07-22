/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.cache;

import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.Geometry;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.LatLng;

public class SearchResKey {
    private final String operationName;

    private String searchString;
    private Geometry locationBias;
    private LatLng location;
    private String placeId;
    private String address;
    private String country;
    private String city;
    private String postcode;
    private String preferredCity;
    private String preferredCountry;

    public SearchResKey(String operationName) {
        this.operationName = operationName;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public Geometry getLocationBias() {
        return locationBias;
    }

    public void setLocationBias(Geometry locationBias) {
        this.locationBias = locationBias;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getPreferredCity() {
        return preferredCity;
    }

    public void setPreferredCity(String preferredCity) {
        this.preferredCity = preferredCity;
    }

    public String getPreferredCountry() {
        return preferredCountry;
    }

    public void setPreferredCountry(String preferredCountry) {
        this.preferredCountry = preferredCountry;
    }
}