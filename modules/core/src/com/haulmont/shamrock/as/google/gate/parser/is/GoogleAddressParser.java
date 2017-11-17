/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.is;

import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.dto.enums.GType;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Parser("IS")
public class GoogleAddressParser extends DefaultGoogleAddressParser {
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

        if (StringUtils.isBlank(address) && StringUtils.isNotBlank(formattedAddress)) {
            if (StringUtils.isNotBlank(ctx.city)) {
                int idx = formattedAddress.toUpperCase().indexOf(", " + StringUtils.upperCase(ctx.city) + ",");
                if (idx < 0) {
                    idx = formattedAddress.toUpperCase().indexOf(" " + StringUtils.upperCase(ctx.city) + ",");
                }

                if (idx > 0)
                    address = formattedAddress.substring(0, idx);
            }

            if (StringUtils.isBlank(address)) {
                String city = getFirstLong(components, GElement.locality, GElement.political);
                if (StringUtils.isNotBlank(city)) {
                    int idx = formattedAddress.toUpperCase().indexOf(", " + StringUtils.upperCase(city) + ",");
                    if (idx > 0)
                        address = formattedAddress.substring(0, idx);
                } else {
                    city = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
                    if (StringUtils.isNotBlank(city)) {
                        int idx = formattedAddress.toUpperCase().indexOf(", " + StringUtils.upperCase(city) + ",");
                        if (idx > 0)
                            address = formattedAddress.substring(0, idx);
                    } else {
                        city = getFirstLong(components, GElement.administrative_area_level_2, GElement.political);
                        int idx = formattedAddress.toUpperCase().indexOf(", " + StringUtils.upperCase(city) + ",");
                        if (idx > 0)
                            address = formattedAddress.substring(0, idx);
                    }
                }
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
}
