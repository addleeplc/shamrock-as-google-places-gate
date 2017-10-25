/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.be;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;

import java.util.Map;

@Parser("BE")
public class GoogleAddressParser extends DefaultGoogleAddressParser {

    public GoogleAddressParser() {
        super("BE");
    }

    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        return getFirstLong(components, GElement.administrative_area_level_1, GElement.locality);
    }

    @Override
    protected String getCountry(Map<String, AddressComponent> components) {
        return "BE";
    }
}
