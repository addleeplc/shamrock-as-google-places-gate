/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.eg;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;

import java.util.Map;

@Parser("EG")
public class GoogleAddressParser extends DefaultGoogleAddressParser {
    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String city = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
        if ("Cairo Governorate".equals(city)) {
            city = "Cairo";
        } else if ("Giza Governorate".equals(city)) {
            city = "Giza";
        }

        return city;
    }
}
