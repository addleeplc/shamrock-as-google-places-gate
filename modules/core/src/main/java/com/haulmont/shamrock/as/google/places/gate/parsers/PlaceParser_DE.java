/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.parsers;

import com.haulmont.shamrock.as.dto.AddressComponents;
import com.haulmont.shamrock.as.google.places.gate.dto.Place;
import com.haulmont.shamrock.geo.utils.PostalCodeUtils;
import org.picocontainer.annotations.Component;

@Component
@PlaceParser.Component({PlaceParser_DE.COUNTRY_NAME})
public class PlaceParser_DE extends AbstractPlaceParser {

    public static final String ISO_COUNTRY_CODE = "DE";

    //

    public static final String COUNTRY_NAME = "Germany";

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

        String postcode = PostalCodeUtils.DE.parse(part);
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
        components.setAddress(getAddress(place, concat(parts, parts.length - 2)));

        if (isBusinessName(place)) {
            components.setCompany(place.getDisplayName() != null ? place.getDisplayName().getText() : "");
        }

        return components;
    }
}
