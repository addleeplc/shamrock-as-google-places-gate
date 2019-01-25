/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.converters;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;

import java.util.Map;

@Component
@PlaceDetailsConverter.Component(country = "CA")
public class PlaceDetailsConverter_CA extends DefaultPlaceDetailsConverter {
    @Override
    protected String parsePostcode(Map<String, AddressComponent> components) {
        String postcode = super.parsePostcode(components);
        String stateValue = getFirstShort(components, GElement.administrative_area_level_1);

        if (StringUtils.isNotBlank(postcode) && StringUtils.isNotBlank(stateValue) && !StringUtils.containsIgnoreCase(postcode, stateValue)) {
            postcode = stateValue + " " + postcode;
        }

        return postcode;
    }
}
