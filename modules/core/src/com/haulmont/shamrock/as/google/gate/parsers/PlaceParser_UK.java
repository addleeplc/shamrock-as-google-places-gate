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
import org.apache.commons.collections.CollectionUtils;
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
            String s = getSubstring(formattedAddress, COUNTRY_SUFFIX);

            String postcode = PostcodeHelper.parsePostcode(s);
            if (postcode != null) {
                if (s.endsWith(postcode)) {
                    s = getSubstring(s, postcode);

                    String buildingAddress = AddressHelper.parseBuildingAddress(s);
                    if (buildingAddress != null) {
                        int i = s.indexOf(buildingAddress);
                        if (i >= 0) {
                            String o = s.substring(i + buildingAddress.length() + COMPONENTS_DIVIDER.length());
                            String[] parts = o.split(COMPONENTS_DIVIDER);
                            if (parts.length == 1 || parts.length == 2) {
                                AddressComponents components = new AddressComponents();

                                components.setAddress(getAddress(place, s.substring(0, i + buildingAddress.length())));
                                components.setCity(StringUtils.trim(parts[parts.length - 1]));
                                components.setPostcode(postcode);
                                components.setCountry("GB");
                                components.setStreet(AddressHelper.parseStreetName(buildingAddress, AddressHelper.ParseAccuracy.MEDIUM));

                                if (isBusinessName(place)) {
                                    components.setCompany(place.getName());
                                }

                                return components;
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
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
}
