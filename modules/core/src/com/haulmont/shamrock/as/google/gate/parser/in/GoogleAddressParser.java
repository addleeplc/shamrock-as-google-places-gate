/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.in;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Parser("IN")
public class GoogleAddressParser extends DefaultGoogleAddressParser {
    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String city = getFirstLong(components, GElement.locality, GElement.political);
        if (StringUtils.isBlank(city))
            city = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);

        if ("New Delhi".equals(city))
            city = "Delhi";

        return city;
    }
}
