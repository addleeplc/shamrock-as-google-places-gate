/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.converters;

import com.haulmont.shamrock.as.google.places.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.places.gate.dto.enums.GElement;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;

import java.util.Map;

@Component
@PlaceDetailsConverter.Component(country = "GB")
public class PlaceDetailsConverter_GB extends DefaultPlaceDetailsConverter {

    private static final String LONDON = "London";

    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String city = getFirstLong(components, GElement.administrative_area_level_2);
        if (StringUtils.isNotBlank(city) && StringUtils.equalsIgnoreCase(city, "Greater London")) {
            city = LONDON;
        } else {
            city = getFirstLong(components, GElement.locality, GElement.postal_town);
            if (StringUtils.isBlank(city))
                city = getFirstLong(components, GElement.locality, GElement.political);

            if (city != null) {
                if (city.equalsIgnoreCase("Greater London")) {
                    city = LONDON;
                }
            } else {
                city = getFirstLong(components, GElement.administrative_area_level_1);
            }
        }

        if (StringUtils.equalsIgnoreCase("City Of London", city)) {
            city = LONDON;
        }

        return city;
    }
}
