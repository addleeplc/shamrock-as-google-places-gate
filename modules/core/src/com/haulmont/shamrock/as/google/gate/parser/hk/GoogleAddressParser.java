/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.hk;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;

import java.util.Map;

@Parser("HK")
public class GoogleAddressParser extends DefaultGoogleAddressParser {

    public GoogleAddressParser() {
        super("HK");
    }

    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        return "Hong Kong";
    }

    @Override
    protected String getCountry(Map<String, AddressComponent> components) {
        return "HK";
    }
}
