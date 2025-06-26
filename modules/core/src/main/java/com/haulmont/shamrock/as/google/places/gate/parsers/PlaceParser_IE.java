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

import java.util.Arrays;
import java.util.regex.Pattern;

@Component
@PlaceParser.Component({PlaceParser_IE.COUNTRY_NAME})
public class PlaceParser_IE extends AbstractPlaceParser {

    public static final String ISO_COUNTRY_CODE = "IE";

    //

    public static final String COUNTRY_NAME = "Ireland";

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

        String postcode = PostalCodeUtils.IE.parse(part);
        if (postcode != null) {
            if (part.equals(postcode)) {
                components.setPostcode(postcode);

                return parseAddressComponents(place, Arrays.copyOfRange(parts, 0, parts.length - 1), components);
            } else {
                return null;
            }
        } else {
            return parseAddressComponents(place, parts, components);
        }
    }

    private AddressComponents parseAddressComponents(Place place, String[] parts, AddressComponents components) {
        if (parts.length < 2) return null;

        String part = parts[parts.length - 1].trim();

        String prefix = "Co.";
        if (part.startsWith(prefix)) {
            String county = part.substring(prefix.length()).trim();

            __parseComponents(place, Arrays.copyOfRange(parts, 0, parts.length - 1), components);
        } else {
            prefix = "County ";
            if (part.startsWith(prefix)) {
                String county = part.substring(prefix.length()).trim();

                __parseComponents(place, Arrays.copyOfRange(parts, 0, parts.length - 1), components);
            } else {
                __parseComponents(place, parts, components);
            }
        }


        if (isBusinessName(place)) {
            components.setCompany(place.getDisplayName() != null ? place.getDisplayName().getText() : "");
        }

        return components;
    }

    private final static Pattern DUBLIN = Pattern.compile("\\b" + "Dublin( [0-9])?" + "\\b", Pattern.CASE_INSENSITIVE);

    private void __parseComponents(Place place, String[] parts, AddressComponents components) {

        String city = parts[parts.length - 1].trim();
        if (DUBLIN.matcher(city).matches()) {
            city = "Dublin";
        }
        components.setCity(city);

        String address = getAddress(place, concat(parts, parts.length - 1));
        components.setAddress(address);
    }

}
