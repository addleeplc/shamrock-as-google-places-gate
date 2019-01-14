/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.utils;

import com.haulmont.shamrock.address.gis.Geometry;

public class CityGeometry extends Geometry {
    private String country;

    public CityGeometry(String country) {
        this.country = country;
    }

    public CityGeometry(Type type, String country) {
        super(type);
        this.country = country;
    }

    public CityGeometry(Type type, double[][] coordinates, String country) {
        super(type, coordinates);
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
