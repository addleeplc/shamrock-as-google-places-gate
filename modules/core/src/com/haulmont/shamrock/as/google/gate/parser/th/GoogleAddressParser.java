/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.th;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;

import java.util.Map;

@Parser("TH")
public class GoogleAddressParser extends DefaultGoogleAddressParser {

    public GoogleAddressParser() {
        super("TH");
    }

    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String cityValue = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
        if ("Krung Thep Maha Nakhon".equals(cityValue) || "Krung Thep".equals(cityValue))
            cityValue = "Bangkok";
        
        return cityValue;
    }

    @Override
    protected String getCountry(Map<String, AddressComponent> components) {
        return "TH";
    }
}
