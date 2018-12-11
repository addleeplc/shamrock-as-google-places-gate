/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.utils;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.Geometry;
import com.haulmont.shamrock.as.google.gate.dto.Location;
import com.haulmont.shamrock.as.google.gate.dto.Place;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GoogleAddressUtils {

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

    public static String getFirstShort(Map<String, AddressComponent> components, GElement... elements) {
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

    public static com.haulmont.shamrock.address.Location convert(Geometry geometry) {
        if (geometry != null) {
            Location location = geometry.getLocation();
            if (location != null) {
                com.haulmont.shamrock.address.Location l = new com.haulmont.shamrock.address.Location();
                l.setLat(location.getLat());
                l.setLon(location.getLng());

                return l;
            }
        }

        return null;
    }

    public static String getFormattedAddress(Place place) {
        String formattedAddress = StringUtils.isBlank(place.getFormattedAddress()) ? place.getVicinity() : place.getFormattedAddress();

        List<String> types = place.getTypes();
        if (types != null) {
            if (types.contains("street_address")) {
                return formattedAddress;
            } else {
                return concat(place.getName(), formattedAddress);
            }
        } else {
            return concat(place.getName(), formattedAddress);
        }
    }

    private static String concat(String name, String address) {
        return StringUtils.isBlank(name) ? address : (name + ", " + address);
    }
}
