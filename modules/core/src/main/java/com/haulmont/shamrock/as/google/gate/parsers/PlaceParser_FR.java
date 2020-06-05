/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parsers;

import com.haulmont.shamrock.as.dto.AddressComponents;
import com.haulmont.shamrock.as.google.gate.dto.Place;
import com.haulmont.shamrock.geo.utils.PostalCodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;

import java.util.List;

@Component
@PlaceParser.Component({PlaceParser_FR.COUNTRY_NAME})
public class PlaceParser_FR extends AbstractPlaceParser {

    public static final String ISO_COUNTRY_CODE = "FR";

    //

    public static final String COUNTRY_NAME = "France";

    private static final String COUNTRY_NAME_SUFFIX = COMPONENTS_DIVIDER + COUNTRY_NAME;

    //

    public AddressComponents parse(Place place) {
        String formattedAddress = place.getFormattedAddress();

        String suffix = COUNTRY_NAME_SUFFIX;
        if (formattedAddress.endsWith(suffix)) {
            return __parse(place, suffix);
        } else {
            return null;
        }
    }

    private AddressComponents __parse(Place place, String suffix) {
        String formattedAddress = place.getFormattedAddress();

        AddressComponents components = new AddressComponents();
        components.setCountry(ISO_COUNTRY_CODE);

        String s = getSubstring(formattedAddress, suffix);

        String[] parts = s.split(COMPONENTS_DIVIDER);
        String part = parts[parts.length - 1];

        String postcode = PostalCodeUtils.FR.parse(part);
        if (postcode != null) {
            if (part.startsWith(postcode)) {
                components.setPostcode(postcode);

                parts[parts.length - 1] = part.substring(postcode.length()).trim();

                return parseAddressComponents(place, parts, components);
            } else {
                return null;
            }
        } else {
            return parseAddressComponents(place, parts, components);
        }
    }

    private AddressComponents parseAddressComponents(Place place, String[] parts, AddressComponents components) {
        if (parts.length < 2) return null;

        String city = parts[parts.length - 1].trim();

        components.setCity(city);
        components.setAddress(getAddress(place, concat(parts, parts.length - 1)));

        if (isBusinessName(place)) {
            components.setCompany(place.getName());
        }

        return components;
    }
}
