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
@PlaceDetailsConverter.Component(country = "EG")
public class PlaceDetailsConverter_EG extends DefaultPlaceDetailsConverter {
    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String city = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
        if ("Cairo Governorate".equals(city)) {
            city = "Cairo";
        } else if ("Giza Governorate".equals(city)) {
            city = "Giza";
        }

        return city;
    }
}
