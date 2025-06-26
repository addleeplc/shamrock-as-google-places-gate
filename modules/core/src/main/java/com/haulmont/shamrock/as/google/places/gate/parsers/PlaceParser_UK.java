/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.parsers;

import com.haulmont.shamrock.as.commons.parsers.AddressComponentsParser;
import com.haulmont.shamrock.as.commons.parsers.AddressComponentsParser_EN;
import com.haulmont.shamrock.as.dto.AddressComponents;
import com.haulmont.shamrock.as.google.places.gate.dto.Place;
import com.haulmont.shamrock.geo.utils.PostalCodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;

@Component
@PlaceParser.Component({PlaceParser_UK.COUNTRY_CODE, PlaceParser_UK.COUNTRY_NAME})
public class PlaceParser_UK extends AbstractPlaceParser {

    public static final String ISO_COUNTRY_CODE = "GB";

    //

    public static final String COUNTRY_CODE = "UK";
    public static final String COUNTRY_NAME = "United Kingdom";

    private static final String COUNTRY_CODE_SUFFIX = COMPONENTS_DIVIDER + COUNTRY_CODE;
    private static final String COUNTRY_NAME_SUFFIX = COMPONENTS_DIVIDER + COUNTRY_NAME;

    //

    private AddressComponentsParser componentsParser = new AddressComponentsParser_EN();

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

        String postcode = PostalCodeUtils.GB.parse(part);
        if (postcode != null) {
            if (part.endsWith(postcode)) {
                components.setPostcode(postcode);

                parts[parts.length - 1] = part.substring(0, part.length() - postcode.length()).trim();

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

        components.setCity(parts[parts.length - 1].trim());

        String part;
        String streetName;

        part = parts[parts.length - 2];
        streetName = parseStreetName(part);
        if (StringUtils.isNotBlank(streetName)
                && part.trim().endsWith(streetName)
                && ((parts.length == 2 && !StringUtils.endsWithIgnoreCase(streetName, part)) || (parts.length > 2)))
        {
            components.setStreet(streetName);

            components.setAddress(getAddress(place, concat(parts, parts.length - 1)));

            if (isBusinessName(place)) {
                components.setCompany(place.getDisplayName() != null ? place.getDisplayName().getText() : "");
            }

            return components;
        } else if (parts.length > 2) {
            part = parts[parts.length - 3];
            streetName = parseStreetName(part);
            if (StringUtils.isNotBlank(streetName)
                    && part.trim().endsWith(streetName)
                    && ((parts.length == 3 && !StringUtils.endsWithIgnoreCase(streetName, part)) || (parts.length > 3)))
            {
                components.setStreet(streetName);

                components.setAddress(getAddress(place, concat(parts, parts.length - 2)));

                if (isBusinessName(place)) {
                    components.setCompany(place.getDisplayName() != null ? place.getDisplayName().getText() : "");
                }

                return components;
            } else {
                return components;
            }
        } else {
            return components;
        }
    }

    private String parseStreetName(String part) {
        AddressComponentsParser.Part parsedStreetName = componentsParser.parseStreetName(new String[]{part});
        return parsedStreetName != null ? parsedStreetName.getMatchedPart() : null;
    }
}
