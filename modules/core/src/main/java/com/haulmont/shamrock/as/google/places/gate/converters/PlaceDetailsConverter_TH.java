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
@PlaceDetailsConverter.Component(country = "TH")
public class PlaceDetailsConverter_TH extends DefaultPlaceDetailsConverter {
    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String cityValue = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
        if ("Krung Thep Maha Nakhon".equals(cityValue) || "Krung Thep".equals(cityValue))
            cityValue = "Bangkok";
        
        return cityValue;
    }
}
