/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parsers;

import com.haulmont.shamrock.address.Address;
import com.haulmont.shamrock.address.AddressComponents;
import com.haulmont.shamrock.address.AddressData;
import com.haulmont.shamrock.as.google.gate.dto.Place;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlaceParsingService {
    @Inject
    private Logger logger;

    private List<PlaceParser> parsers = new ArrayList<>();

    public Address parse(Place place, String source) {
        for (PlaceParser parser : parsers) {
            try {
                AddressComponents components = parser.parse(place);
                if (components != null) {
                    Address res = new Address();
                    AddressData data = new AddressData();
                    res.setAddressData(data);

                    String formattedAddress = GoogleAddressUtils.getFormattedAddress(place);
                    data.setFormattedAddress(formattedAddress);

                    data.setAddressComponents(components);
                    data.setLocation(GoogleAddressUtils.convert(place.getGeometry()));

                    res.setId(String.format("%s|%s", source, place.getPlaceId()));
                    res.setRefined(false);

                    return res;
                }
            } catch (Exception e) {
                logger.warn(String.format("Failed to parse address '%s': %s", GoogleAddressUtils.getFormattedAddress(place), e.getMessage()));
            }
        }

        return null;
    }

    public void register(PlaceParser parser) {
        parsers.add(parser);
    }
}
