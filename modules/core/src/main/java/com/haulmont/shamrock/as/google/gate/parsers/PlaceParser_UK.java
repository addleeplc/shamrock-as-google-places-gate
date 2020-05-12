/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parsers;

import com.haulmont.shamrock.address.AddressComponents;
import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.as.google.gate.dto.Place;
import com.haulmont.shamrock.geo.PostcodeHelper;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;

import java.util.List;

@Component
@PlaceParser.Component
public class PlaceParser_UK implements PlaceParser {
    private static final String COMPONENTS_DIVIDER = ", ";
    private static final String COUNTRY_SUFFIX = COMPONENTS_DIVIDER + "UK";

    public AddressComponents parse(Place place) {
        String formattedAddress = place.getFormattedAddress();

        if (formattedAddress.endsWith(COUNTRY_SUFFIX)) {
            AddressComponents components = new AddressComponents();
            components.setCountry("GB");

            String s = getSubstring(formattedAddress, COUNTRY_SUFFIX);

            String postcode = PostcodeHelper.parsePostcode(s);
            if (postcode != null) {
                if (s.endsWith(postcode)) {
                    components.setPostcode(postcode);

                    s = getSubstring(s, postcode);

                    return parseAddressComponents(place, s, components);
                } else {
                    return null;
                }
            } else {
                return parseAddressComponents(place, s, components);
            }
        } else {
            return null;
        }
    }

    private AddressComponents parseAddressComponents(Place place, String s, AddressComponents components) {
        String[] parts = s.split(COMPONENTS_DIVIDER);
        if (parts.length < 2) return null;

        components.setCity(parts[parts.length - 1].trim());

        String part;
        String streetName;

        part = parts[parts.length - 2];
        streetName = AddressHelper.parseStreetName(part, AddressHelper.ParseAccuracy.LOW);
        if (StringUtils.isNotBlank(streetName) && part.trim().endsWith(streetName)) {
            components.setStreet(streetName);

            components.setAddress(getAddress(place, concat(parts, parts.length - 1)));

            if (isBusinessName(place)) {
                components.setCompany(place.getName());
            }

            return components;
        } else if (parts.length > 2) {
            part = parts[parts.length - 3];
            streetName = AddressHelper.parseStreetName(part, AddressHelper.ParseAccuracy.LOW);
            if (StringUtils.isNotBlank(streetName) && part.trim().endsWith(streetName)) {
                components.setStreet(streetName);

                components.setAddress(getAddress(place, concat(parts, parts.length - 2)));

                if (isBusinessName(place)) {
                    components.setCompany(place.getName());
                }

                return components;
            } else {
                return components;
            }
        } else {
            return components;
        }
    }

    private boolean isBusinessName(Place place) {
        return place.getTypes() != null && place.getTypes().contains("establishment");
    }

    private String getSubstring(String s, String suffix) {
        return s.substring(0, s.length() - suffix.length());
    }

    private static String getAddress(Place place, String address) {
        List<String> types = place.getTypes();
        if (types != null) {
            if (types.contains("street_address")) {
                return address;
            } else {
                return concat(place.getName(), address);
            }
        } else {
            return concat(place.getName(), address);
        }
    }

    private static String concat(String name, String address) {
        return StringUtils.isBlank(name) ? address : (name + ", " + address);
    }

    private static String concat(String[] parts, int li) {
        StringBuilder builder = new StringBuilder();

        int i = 0;
        for (; i < li - 1; i++) {
            builder.append(parts[i].trim()).append(COMPONENTS_DIVIDER);
        }
        builder.append(parts[i]);

        return builder.toString();
    }
}
