/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.converters;

import com.haulmont.shamrock.as.google.places.gate.dto.AddressComponent;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;

import java.util.Map;

@Component
@PlaceDetailsConverter.Component(country = "FR")
public class PlaceDetailsConverter_FR extends DefaultPlaceDetailsConverter {
    private static final String[] PARIS_POSTCODE_AREAS = {
            "75", // Paris
            "91", // Vigneux-sur-Seine
            "92", // Hauts-de-Seine
            "93", // Seine-Saint-Denis
            "94" // Val-de-Marne
    };

    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String postcode = parsePostcode(components);

        if (StringUtils.startsWithAny(postcode, PARIS_POSTCODE_AREAS))
            return "Paris";
        else
            return super.parseCity(components);
    }
}
