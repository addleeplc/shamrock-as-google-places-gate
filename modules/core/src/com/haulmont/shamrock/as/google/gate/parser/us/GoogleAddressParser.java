/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.us;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Parser("US")
public class GoogleAddressParser extends DefaultGoogleAddressParser {
    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String city = getFirstLong(components, GElement.locality, GElement.postal_town, GElement.sublocality, GElement.neighborhood);
        if (city != null &&
                (city.equals("NY") ||
                        "Manhattan".equalsIgnoreCase(city) ||
                        "Brooklyn".equalsIgnoreCase(city) ||
                        "Queens".equalsIgnoreCase(city) ||
                        "Staten Island".equalsIgnoreCase(city) ||
                        "The Bronx".equalsIgnoreCase(city) ||
                        "Bronx".equalsIgnoreCase(city))
                ) {
            city = "New York";
        }

        return city;
    }

    @Override
    protected String parsePostcode(Map<String, AddressComponent> components) {
        String postcode = super.parsePostcode(components);
        String stateValue = getFirstShort(components, GElement.administrative_area_level_1);

        if (StringUtils.isNotBlank(postcode) && StringUtils.isNotBlank(stateValue) && !StringUtils.containsIgnoreCase(postcode, stateValue)) {
            postcode = stateValue + " " + postcode;
        }

        return postcode;
    }

    @Override
    protected String parseAddress(String formattedAddress, Map<String, AddressComponent> components, List<String> types, ParseAddressContext ctx) {
        String address = super.parseAddress(formattedAddress, components, types, ctx);

        String cityRegion = getFirstLong(components, GElement.sublocality, GElement.sublocality_level_1);
        if (StringUtils.isBlank(cityRegion)) return address;

        if ("New York".equals(ctx.city)) {
            if ("Manhattan".equalsIgnoreCase(cityRegion)) {
                cityRegion = "Manhattan";
            } else if ("Brooklyn".equalsIgnoreCase(cityRegion)) {
                cityRegion = "Brooklyn";
            } else if ("Queens".equalsIgnoreCase(cityRegion)) {
                cityRegion = "Queens";
            } else if ("Staten Island".equalsIgnoreCase(cityRegion)) {
                cityRegion = "Staten Island";
            } else if ("The Bronx".equalsIgnoreCase(cityRegion) || "Bronx".equalsIgnoreCase(cityRegion)) {
                cityRegion = "Bronx";
            }
        }

        return StringUtils.isNotBlank(cityRegion) ? address + ", " + cityRegion : address;
    }

    @Override
    protected String parseBuildingName(String placeName, Map<String, AddressComponent> components, List<String> types) {
        String buildingName = getFirstLong(components, GElement.premise, GElement.subpremise);
        if (StringUtils.isBlank(buildingName) && StringUtils.isNotBlank(placeName)) {
            if (CollectionUtils.containsAny(types, Arrays.asList(GElement.premise.name(), GElement.subpremise.name())))
                buildingName = placeName.replace(", ", " ").replace(",", " ");

            AddressComponent route = components.get(GElement.route.name());
            if (route != null) {
                AddressComponent streetNumber = components.get(GElement.street_number);

                String street;
                if (streetNumber != null) {
                    street = streetNumber.getLongName() + " " + route.getShortName();
                } else {
                    street = route.getShortName();
                }

                if (StringUtils.equalsIgnoreCase(buildingName, street) || StringUtils.containsIgnoreCase(buildingName, street) || StringUtils.containsIgnoreCase(street, buildingName))
                    buildingName = null;
            }
        }

        return buildingName;
    }
}
