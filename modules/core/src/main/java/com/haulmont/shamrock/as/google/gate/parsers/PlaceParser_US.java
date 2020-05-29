/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parsers;

import com.haulmont.shamrock.address.AddressComponents;
import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.as.google.gate.dto.Place;
import com.haulmont.shamrock.geo.utils.PostalCodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;

@Component
@PlaceParser.Component({PlaceParser_US.COUNTRY_CODE, PlaceParser_US.COUNTRY_NAME})
public class PlaceParser_US extends AbstractPlaceParser {

    public static final String ISO_COUNTRY_CODE = "US";

    //

    public static final String COUNTRY_CODE = "USA";
    public static final String COUNTRY_NAME = "United States";

    private static final String COUNTRY_CODE_SUFFIX = COMPONENTS_DIVIDER + COUNTRY_CODE;
    private static final String COUNTRY_NAME_SUFFIX = COMPONENTS_DIVIDER + COUNTRY_NAME;

    //

    public AddressComponents parse(Place place) {
        String formattedAddress = place.getFormattedAddress();

        String suffix = COUNTRY_CODE_SUFFIX;
        if (formattedAddress.endsWith(suffix)) {
            return __parse(place, suffix);
        } else {
            suffix = COUNTRY_NAME_SUFFIX;
            if (formattedAddress.endsWith(suffix)) {
                return __parse(place, suffix);
            } else {
                return null;
            }
        }
    }

    private AddressComponents __parse(Place place, String suffix) {
        String formattedAddress = place.getFormattedAddress();

        AddressComponents components = new AddressComponents();
        components.setCountry(ISO_COUNTRY_CODE);

        String s = getSubstring(formattedAddress, suffix);

        String[] parts = s.split(COMPONENTS_DIVIDER);
        String part = parts[parts.length - 1];

        String postcode = PostalCodeUtils.US.parse(part);
        if (postcode != null) {
            if (s.endsWith(postcode)) {
                components.setPostcode(postcode);

                parts[parts.length - 1] = part.substring(0, part.length() - postcode.length()).trim();

                return parseAddressComponents(place, s, components);
            } else {
                return null;
            }
        } else {
            return parseAddressComponents(place, s, components);
        }
    }

    private AddressComponents parseAddressComponents(Place place, String s, AddressComponents components) {
        String[] parts = s.split(COMPONENTS_DIVIDER);
        if (parts.length < 3) return null;

        String state = parts[parts.length - 1].trim();
        String city = parts[parts.length - 2].trim();

        components.setCity(city);

        String part;
        String streetName;

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
    }

}
