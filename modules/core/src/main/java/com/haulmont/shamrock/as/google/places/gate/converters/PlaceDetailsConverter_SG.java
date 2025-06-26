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
@PlaceDetailsConverter.Component(country = "SG")
public class PlaceDetailsConverter_SG extends DefaultPlaceDetailsConverter {
    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        return "Singapore";
    }
}
