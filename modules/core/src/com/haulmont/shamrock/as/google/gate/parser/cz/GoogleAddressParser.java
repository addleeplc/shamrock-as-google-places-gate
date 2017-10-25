/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.cz;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;

import java.util.Map;

@Parser("CZ")
public class GoogleAddressParser extends DefaultGoogleAddressParser {

    public GoogleAddressParser() {
        super("CZ");
    }

    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String city = getFirstLong(components, GElement.sublocality_level_1, GElement.sublocality, GElement.political);
        if (city.matches("Praha\\s[0-9]+")) {
            city = "Prague";
        } else {
            city = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
            if ("Hlavní město Praha".equals(city))
                city = "Prague";
        }

        return city;
    }

    @Override
    protected String getCountry(Map<String, AddressComponent> components) {
        return "CZ";
    }
}
