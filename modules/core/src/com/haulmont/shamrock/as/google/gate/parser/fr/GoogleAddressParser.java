/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.fr;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Parser("FR")
public class GoogleAddressParser extends DefaultGoogleAddressParser {
    private static final String[] PARIS_POSTCODE_AREAS = {
            "75", // Paris
            "92", // Hauts-de-Seine
            "93", // Seine-Saint-Denis
            "94" // Val-de-Marne
    };

    public GoogleAddressParser() {
        super("FR");
    }

    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String postcode = parsePostcode(components);

        if (StringUtils.startsWithAny(postcode, PARIS_POSTCODE_AREAS))
            return "Paris";
        else
            return super.parseCity(components);
    }

    @Override
    protected String getCountry(Map<String, AddressComponent> components) {
        return "FR";
    }
}
