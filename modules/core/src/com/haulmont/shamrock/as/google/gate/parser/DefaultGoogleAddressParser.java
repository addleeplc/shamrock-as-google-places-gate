/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser;

import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.dto.enums.GType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Parser
public class DefaultGoogleAddressParser extends AbstractGoogleAddressParser {
    @Override
    protected void prepareComponents(Map<String, AddressComponent> components) {
        sanitizeAddress(components);
    }

    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        return getFirstLong(components, GElement.locality, GElement.postal_town);
    }

    @Override
    protected String parsePostcode(Map<String, AddressComponent> components) {
        return getFirstLong(components, GElement.postal_code);
    }

    @Override
    protected String parseBuildingName(String placeName, Map<String, AddressComponent> components, List<String> types) {
        String buildingName = getFirstLong(components, GElement.premise, GElement.subpremise);
        if (StringUtils.isBlank(buildingName) && StringUtils.isNotBlank(placeName)) {
            if (CollectionUtils.containsAny(types, Arrays.asList(GElement.premise.name(), GElement.subpremise.name())))
                buildingName = placeName.replace(", ", " ")
                        .replace(",", " ");
        }

        return buildingName;
    }

    @Override
    protected String parseCompanyName(String placeName, Map<String, AddressComponent> components, List<String> types) {
        if (!CollectionUtils.containsAny(types, Arrays.asList(GElement.street_address.name(), GElement.premise.name(), GElement.subpremise.name()))
                && StringUtils.isNotBlank(placeName)) {
            return placeName.replace(", ", " ")
                    .replace(",", " ");
        } else {
            return null;
        }
    }

    @Override
    protected String parseBuildingNumber(Map<String, AddressComponent> components) {
        return getFirstLong(components, GElement.street_number);
    }

    @Override
    protected String parseStreet(String formattedAddress, Map<String, AddressComponent> components) {
        String street = getFirstLong(components, GElement.route);
        if (StringUtils.isBlank(street))
            street = AddressHelper.parseStreetName(formattedAddress, AddressHelper.ParseAccuracy.HIGH);

        if (StringUtils.isBlank(street))
            street = AddressHelper.parseStreetName(formattedAddress, AddressHelper.ParseAccuracy.LOW);

        return street;
    }

    @Override
    protected String parseAddress(String formattedAddress, Map<String, AddressComponent> components, List<String> types, ParseAddressContext ctx) {
        String address;

        String publicTransportStop = getFirstLong(components, GElement.train_station, GElement.transit_station, GElement.bus_station, GElement.subway_station);
        if (StringUtils.isNotBlank(publicTransportStop)) {
            address = publicTransportStop;
        } else {
            String poi = getFirstLong(components, GElement.airport, GElement.park, GElement.point_of_interest);
            if (StringUtils.isNotBlank(poi)) {
                address = poi;
            } else if (isType(types, GType.intersection)) {
                String interValue;
                interValue = StringUtils.substringBefore(formattedAddress, ",");
                if (StringUtils.isBlank(interValue))
                    interValue = formattedAddress;

                address = interValue;
            } else {
                String street = ctx.street;
                if (StringUtils.isBlank(street)) {
                    address = AddressHelper.parseBuildingAddress(formattedAddress);

                    if (StringUtils.isBlank(address))
                        address = getFirstLong(components, GElement.establishment);
                } else {
                    String buildingNumber = ctx.buildingNumber;
                    if (StringUtils.isNotBlank(buildingNumber))
                        address = buildingNumber + " " + street;
                    else
                        address = street;
                }
            }
        }

        if (StringUtils.isNotBlank(formattedAddress) && StringUtils.isBlank(address)) {
            if (StringUtils.isNotBlank(ctx.city)) {
                int idx = formattedAddress.toUpperCase().indexOf(", " + StringUtils.upperCase(ctx.city) + ",");
                if (idx > 0)
                    address = formattedAddress.substring(0, idx);
                else
                    address = formattedAddress;
            }
        }

        if (StringUtils.isBlank(address)) return null;

        String building = ctx.building;
        if (StringUtils.isNotBlank(building) && !StringUtils.containsIgnoreCase(address, building))
            address = building + ", " + address;

        String company = ctx.company;
        if (StringUtils.isNotBlank(company) && !StringUtils.containsIgnoreCase(address, company))
            address = company + ", " + address;

        return address;
    }

    @Override
    protected String getCountry(Map<String, AddressComponent> components) {
        return getFirstShort(components, GElement.country, GElement.political);
    }
}
