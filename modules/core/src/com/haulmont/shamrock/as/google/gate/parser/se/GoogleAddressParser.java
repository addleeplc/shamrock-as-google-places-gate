/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.se;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;

import java.util.Map;

@Parser("SE")
public class GoogleAddressParser extends DefaultGoogleAddressParser {

    public GoogleAddressParser() {
        super("SE");
    }

    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        return getFirstLong(components, GElement.postal_town, GElement.locality);
    }

    @Override
    protected String getCountry(Map<String, AddressComponent> components) {
        return "SE";
    }
}
