/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.converters;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;

import java.util.List;
import java.util.Map;

@Component
@PlaceDetailsConverter.Component(country = "JP")
public class PlaceDetailsConverter_JP extends DefaultPlaceDetailsConverter {
    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        return getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
    }

    @Override
    protected String parseStreet(String formattedAddress, Map<String, AddressComponent> components) {
        String district = getFirstLong(components, GElement.locality, GElement.political);
        String street = getFirstLong(components, GElement.sublocality_level_2, GElement.sublocality, GElement.political);
        if (StringUtils.isBlank(street) && StringUtils.isNotBlank(district))
            return district;
        else if (StringUtils.isBlank(district) && StringUtils.isNotBlank(street))
            return street;
        else if (StringUtils.isNotBlank(district) && StringUtils.isNotBlank(street))
            return street + ", " + district;
        else
            return super.parseStreet(formattedAddress, components);
    }

    @Override
    protected String parseBuildingNumber(String formattedAddress, Map<String, AddressComponent> components) {
        String cityDistrict = getExact(components, true, GElement.sublocality_level_3, GElement.sublocality, GElement.political);
        String cityBlock = getExact(components, true, GElement.sublocality_level_4, GElement.sublocality, GElement.political);
        String buildingNumber = getExact(components, true, GElement.premise);
        if (StringUtils.isBlank(buildingNumber)) return null;

        String res = "";
        if (StringUtils.isNotBlank(cityBlock))
            res = cityBlock + "-" + buildingNumber;

        return StringUtils.isNotBlank(cityDistrict) ? cityDistrict + "-" + res : res;
    }

    @Override
    protected String parseBuildingName(String placeName, Map<String, AddressComponent> components, List<String> types) {
        return null;
    }
}
