/*
 * Copyright
 */

package com.haulmont.shamrock.as.google.gate.parsers;

import com.haulmont.shamrock.as.google.gate.dto.Place;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public abstract class AbstractPlaceParser implements PlaceParser {
    protected static String getAddress(Place place, String address) {
        List<String> types = place.getTypes();
        if (types != null) {
            if (types.contains("street_address")) {
                return address;
            } else {
                return AbstractPlaceParser.concat(place.getName(), address);
            }
        } else {
            return AbstractPlaceParser.concat(place.getName(), address);
        }
    }

    private static String concat(String name, String address) {
        return StringUtils.isBlank(name) ? address : (name + ", " + address);
    }

    protected static String concat(String[] parts, int li) {
        StringBuilder builder = new StringBuilder();

        int i = 0;
        for (; i < li - 1; i++) {
            builder.append(parts[i].trim()).append(COMPONENTS_DIVIDER);
        }
        builder.append(parts[i]);

        return builder.toString();
    }

    protected boolean isBusinessName(Place place) {
        return place.getTypes() != null && place.getTypes().contains("establishment");
    }

    protected String getSubstring(String s, String suffix) {
        return s.substring(0, s.length() - suffix.length());
    }
}
