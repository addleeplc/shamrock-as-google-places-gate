/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.converters;

import com.haulmont.shamrock.address.Address;
import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class PlaceDetailsConverterService {

    @Inject
    private Logger logger;

    @Inject
    private DefaultPlaceDetailsConverter placeDetailsConverter;

    //

    private Map<String, PlaceDetailsConverter> map = Collections.synchronizedMap(new HashMap<>());


    //

    public Address convert(PlaceDetails place, String source) {
        Map<String, AddressComponent> components = GoogleAddressUtils.convert(place.getAddressComponents());

        String country = GoogleAddressUtils.getFirstShort(components, GElement.country, GElement.political);
        if (StringUtils.isBlank(country)) {
            throw new RuntimeException("Country is null");
        }

        try {
            PlaceDetailsConverter converter = getConverter(country);
            Address res = converter.convert(place, components);

            if (res != null) {
                res.setId(String.format("%s|%s", source, place.getPlaceId()));
                res.setRefined(true);
                return res;
            } else {
                return null;
            }
        } catch (Throwable e) {
            logger.warn(String.format("Failed to parse address '%s': %s", GoogleAddressUtils.getFormattedAddress(place), e.getMessage()));
            return null;
        }
    }

    private PlaceDetailsConverter getConverter(String country) {
        PlaceDetailsConverter converter = map.get(country);
        return converter != null ? converter : placeDetailsConverter;
    }

    //

    public void register(String country, PlaceDetailsConverter converter) {
        map.put(country, converter);
    }
}
