/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate;

import com.haulmont.monaco.ServiceException;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.shamrock.as.contexts.GeoRegion;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.google.places.gate.converters.PlaceDetailsConverterService;
import com.haulmont.shamrock.as.google.places.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.places.gate.services.GooglePlacesService;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.LatLng;
import com.haulmont.shamrock.as.google.places.gate.utils.GoogleAddressUtils;
import com.haulmont.shamrock.as.utils.GeoHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SearchNearbyService {

    @Inject
    private Logger logger;

    @Inject
    private GooglePlacesService googlePlacesService;

    @Inject
    private PlaceDetailsConverterService placeDetailsConverterService;

    //

    public List<Address> searchNearby(GeoRegion region) {
        List<Address> res;

        long ts = System.currentTimeMillis();

        try {
            List<PlaceDetails> places = googlePlacesService.getPlaces(region);

            if (CollectionUtils.isEmpty(places))
                res = Collections.emptyList();
            else
                res = convert(places, region);

            String firstAddress;
            if (!CollectionUtils.isEmpty(res) && res.get(0) != null && res.get(0).getAddressData() != null)
                firstAddress = res.get(0).getAddressData().getFormattedAddress();
            else
                firstAddress = "N/A";

            logger.debug("Reverse geocode address by location (loc: {},{}, res: '{}', resSize: {}) ({} ms)",
                    region.getLatitude(), region.getLongitude(), firstAddress, res.size(), System.currentTimeMillis() - ts);

            return res;
        } catch (ServiceException e) {
            throw e;
        } catch (Throwable t) {
            throw new ServiceException(ErrorCode.SERVER_ERROR, "Unknown error", t);
        }
    }

    private List<Address> convert(List<PlaceDetails> results, GeoRegion region) {
        List<Address> res = new ArrayList<>();
        for (PlaceDetails placeDetails : results) {
            try {
                if (placeDetails.getTypes().size() == 1 && placeDetails.getTypes().contains("route")) continue;

                Address address = placeDetailsConverterService.convert(placeDetails);
                address.getAddressData().setFormattedAddress(GoogleAddressUtils.getFormattedAddress(placeDetails));

                LatLng location = placeDetails.getLocation();
                if (location != null && location.getLatitude() != null && location.getLongitude() != null) {
                    double distance = GeoHelper.getGeoDistance(location.getLongitude(), location.getLatitude(), region.getLongitude(), region.getLatitude());
                    if (distance <= region.getRadius()) {
                        address.setDistance(distance);
                        res.add(address);
                    }
                }
            } catch (Throwable e) {
                logger.warn("Fail to parse address: " + (GoogleAddressUtils.getFormattedAddress(placeDetails)), e);
            }
        }

        logger.info(
                String.format(
                        "Search addresses near (%s, %s), radius %s, found: %s",
                        region.getLatitude(), region.getLongitude(), region.getRadius(), res.size()
                )
        );

        return res;
    }
}
