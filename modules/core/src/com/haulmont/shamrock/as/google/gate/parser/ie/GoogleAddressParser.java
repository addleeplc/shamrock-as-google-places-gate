/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.ie;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Parser("IE")
public class GoogleAddressParser extends DefaultGoogleAddressParser {
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
