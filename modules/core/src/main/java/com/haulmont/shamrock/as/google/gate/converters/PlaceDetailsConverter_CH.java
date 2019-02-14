/*
 * Copyright 2008 - 2019 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.converters;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.picocontainer.annotations.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@PlaceDetailsConverter.Component(country = "CH")
public class PlaceDetailsConverter_CH extends DefaultPlaceDetailsConverter {

    private static final Pattern BUILDING_NUMBER_PATTERN = Pattern.compile(
            "([0-9]+)(-[0-9]+)?(-[0-9]+)?$", Pattern.CASE_INSENSITIVE
    );

    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        AddressComponent component = components.get("administrative_area_level_1");
        if (component != null) return component.getLongName();

        component = components.get("administrative_area_level_2");
        if (component != null) return component.getLongName();

        return null;
    }

    @Override
    protected String parseStreet(String formattedAddress, Map<String, AddressComponent> components) {
        String street = super.parseStreet(formattedAddress, components);
        if (StringUtils.isNotBlank(street)) return street;

        Pair<String, String> pair = parseBuildingAndStreet(formattedAddress, components);
        return pair.getRight();
    }

    @Override
    protected String parseBuildingNumber(String formattedAddress, Map<String, AddressComponent> components) {
        String buildingNumber = super.parseBuildingNumber(formattedAddress, components);
        if (StringUtils.isNotBlank(buildingNumber)) return buildingNumber;

        Pair<String, String> pair = parseBuildingAndStreet(formattedAddress, components);
        return pair.getLeft();
    }

    private Pair<String, String> parseBuildingAndStreet(String formattedAddress, Map<String, AddressComponent> components) {
        String address = null;
        String buildingNumber = null;

        String tokens[] = formattedAddress.split(",");
        for (String token : tokens) {
            token = token.trim();

            Matcher matcher = BUILDING_NUMBER_PATTERN.matcher(token);
            if (matcher.find()) {
                buildingNumber = matcher.group();
                address = token;
                break;
            }
        }

        if (address != null) address = address.replace(buildingNumber, "").trim();

        return Pair.of(buildingNumber, address);
    }
}
