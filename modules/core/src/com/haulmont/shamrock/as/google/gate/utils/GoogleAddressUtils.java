/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.utils;

import com.haulmont.monaco.AppContext;
import com.haulmont.shamrock.address.Address;
import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.Geometry;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parser.AbstractGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.AddressParseException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GoogleAddressUtils {
    private static String BASE_PARSERS_PACKAGE = "com.haulmont.shamrock.as.google.gate.parser";

    public static Map<String, AddressComponent> convert(List<AddressComponent> components) {
        Map<String, AddressComponent> res = new HashMap<>();

        for (AddressComponent component : components) {
            for (String key : component.getTypes()) {
                if (key != null) {
                    res.put(key, component);
                }
            }
        }

        return res;
    }

    public static Address parseAddress(String placeName, String formattedAddress, Geometry geometry, Map<String, AddressComponent> components, List<String> types) throws AddressParseException {
        String countryValue = getFirstShort(components, GElement.country, GElement.political);
        if (StringUtils.isBlank(countryValue)) {
            throw new AddressParseException("Country is null");
        }

        return getParser(countryValue).parse(placeName, formattedAddress, geometry, components, types);
    }

    private static String getFirstShort(Map<String, AddressComponent> components, GElement... elements) {
        return getFirst(components, false, elements);
    }

    private static String getFirst(Map<String, AddressComponent> components, boolean isGetLongName, GElement... elements) {

        if (elements == null || elements.length == 0) {
            return null;
        } else {
            for (GElement element : elements) {
                AddressComponent component = components.get(element.toString());

                if (component != null) {

                    if (isGetLongName) {
                        if (StringUtils.isNotBlank(component.getLongName())) {
                            return component.getLongName().trim();
                        }
                    } else {
                        if (StringUtils.isNotBlank(component.getShortName())) {
                            return component.getShortName().trim();
                        }
                    }
                }
            }

            return null;
        }
    }

    private static AbstractGoogleAddressParser getParser(String country) {
        AbstractGoogleAddressParser parser = AppContext.getBean(BASE_PARSERS_PACKAGE + "." + country.toLowerCase());
        if (parser == null)
            return AppContext.getBean(BASE_PARSERS_PACKAGE);
        else
            return parser;
    }
}
