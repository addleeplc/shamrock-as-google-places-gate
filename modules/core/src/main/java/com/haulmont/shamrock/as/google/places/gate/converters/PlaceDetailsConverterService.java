/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.converters;

import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.google.places.gate.constants.GeometryConstants;
import com.haulmont.shamrock.as.google.places.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.places.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.places.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.LatLng;
import com.haulmont.shamrock.as.google.places.gate.utils.GoogleAddressUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class PlaceDetailsConverterService {

    private final Map<String, PlaceDetailsConverter> map = Collections.synchronizedMap(new HashMap<>());
    @Inject
    private Logger logger;

    //
    @Inject
    private DefaultPlaceDetailsConverter placeDetailsConverter;


    //

    public Address convert(PlaceDetails place) {
        Map<String, AddressComponent> components = GoogleAddressUtils.convert(place.getAddressComponents());

        String country = GoogleAddressUtils.getFirstShort(components, GElement.country, GElement.political);
        if (StringUtils.isBlank(country)) {
            throw new RuntimeException("Country is null");
        }

        if (!country.equals("JE")) {
            LatLng location = place.getLocation();
            if (location.getLatitude() != null && location.getLongitude() != null) {
                S2Point p = S2LatLng.fromDegrees(location.getLatitude(), location.getLongitude()).toPoint();
                if (GeometryConstants.JERSEY_POLYGON.contains(p)) country = "JE";
            }

        }

        try {
            PlaceDetailsConverter converter = getConverter(country);
            Address res = converter.convert(place, components);

            if (res != null) {
                res.setId(String.format("google-places|%s", place.getId()));
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
