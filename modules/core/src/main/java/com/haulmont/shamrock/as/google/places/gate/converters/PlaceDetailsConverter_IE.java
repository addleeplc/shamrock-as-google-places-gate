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
@PlaceDetailsConverter.Component(country = "IE")
public class PlaceDetailsConverter_IE extends DefaultPlaceDetailsConverter {
    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String city = getFirstLong(components, GElement.administrative_area_level_1, GElement.administrative_area_level_2, GElement.locality);
        if (StringUtils.containsIgnoreCase(city, "county dublin")) {
            city = "Dublin";
        } else {
            String town = getFirstLong(components, GElement.postal_town);
            if (StringUtils.isNotBlank(town))
                city = town;
        }

        return city;
    }

    @Override
    protected String parsePostcode(Map<String, AddressComponent> components) {
        String postcode = getFirstLong(components, GElement.postal_code);
        if (StringUtils.isBlank(postcode)) {
            postcode = null;
        }

        return postcode;
    }
}
