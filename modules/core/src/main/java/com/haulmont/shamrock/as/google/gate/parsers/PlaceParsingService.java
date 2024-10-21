/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parsers;

import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.dto.AddressComponents;
import com.haulmont.shamrock.as.dto.AddressData;
import com.haulmont.shamrock.as.google.gate.dto.Place;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Component
public class PlaceParsingService {
    @Inject
    private Logger logger;

    private Map<String, PlaceParser> parsers = new HashMap<>();

    public Address parse(Place place, String source) {
        String s = place.getFormattedAddress();

        String[] parts = s.split(PlaceParser.COMPONENTS_DIVIDER);
        String lastPart = parts[parts.length - 1];

        PlaceParser parser = parsers.get(lastPart);
        if (parser == null) {
            logger.debug("No parse registered for '" + lastPart + "'");
            return null;
        }

        try {
            AddressComponents components = parser.parse(place);
            if (components != null && StringUtils.isNotBlank(components.getAddress())) {
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

        return null;
    }

    public void register(String code, PlaceParser parser) {
        parsers.put(code, parser);
    }
}
