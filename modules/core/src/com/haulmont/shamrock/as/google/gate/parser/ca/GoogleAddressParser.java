/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.ca;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Parser("CA")
public class GoogleAddressParser extends DefaultGoogleAddressParser {
    @Override
    protected String parsePostcode(Map<String, AddressComponent> components) {
        String postcode = super.parsePostcode(components);
        String stateValue = getFirstShort(components, GElement.administrative_area_level_1);

        if (StringUtils.isNotBlank(postcode) && StringUtils.isNotBlank(stateValue) && !StringUtils.containsIgnoreCase(postcode, stateValue)) {
            postcode = stateValue + " " + postcode;
        }

        return postcode;
    }
}
