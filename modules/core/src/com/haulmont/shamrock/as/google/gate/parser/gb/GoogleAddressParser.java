/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.gb;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Parser("GB")
public class GoogleAddressParser extends DefaultGoogleAddressParser {

    public GoogleAddressParser() {
        super("GB");
    }

    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String city = getFirstLong(components, GElement.administrative_area_level_2);
        if (StringUtils.isNotBlank(city) && StringUtils.equalsIgnoreCase(city, "Greater London")) {
            city = "London";
        } else {
            city = getFirstLong(components, GElement.locality, GElement.postal_town);
            if (StringUtils.isBlank(city))
                city = getFirstLong(components, GElement.locality, GElement.political);

            if (city != null) {
                if (city.equalsIgnoreCase("Greater London")) {
                    city = "London";
                }
            } else {
                city = getFirstLong(components, GElement.administrative_area_level_1);
            }
        }

        return city;
    }

    @Override
    protected String getCountry(Map<String, AddressComponent> components) {
        return "GB";
    }
}
