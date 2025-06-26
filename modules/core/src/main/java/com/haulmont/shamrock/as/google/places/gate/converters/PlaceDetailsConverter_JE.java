/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.converters;

import com.haulmont.shamrock.as.google.places.gate.dto.AddressComponent;
import org.picocontainer.annotations.Component;

import java.util.Map;

@Component
@PlaceDetailsConverter.Component(country = "JE")
public class PlaceDetailsConverter_JE extends DefaultPlaceDetailsConverter {
    @Override
    protected String getCountry(Map<String, AddressComponent> components) {
        return "JE";
    }
}
