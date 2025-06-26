/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.converters;

import com.haulmont.shamrock.as.google.places.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.places.gate.dto.enums.GElement;
import org.picocontainer.annotations.Component;

import java.util.Map;

@Component
@PlaceDetailsConverter.Component(country = "CZ")
public class PlaceDetailsConverter_CZ extends DefaultPlaceDetailsConverter {
    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String city = getFirstLong(components, GElement.sublocality_level_1, GElement.sublocality, GElement.political);
        if (city.matches("Praha\\s[0-9]+")) {
            city = "Prague";
        } else {
            city = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
            if ("Hlavní město Praha".equals(city))
                city = "Prague";
        }

        return city;
    }
}
