/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate;

import com.haulmont.monaco.ServiceException;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.shamrock.as.commons.parsers.AddressComponentsParser_EN;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.dto.AddressData;
import com.haulmont.shamrock.as.google.places.gate.cache.Converters;
import com.haulmont.shamrock.as.google.places.gate.cache.GeocodeCache;
import com.haulmont.shamrock.as.google.places.gate.converters.PlaceDetailsConverterService;
import com.haulmont.shamrock.as.google.places.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.places.gate.dto.RefineContext;
import com.haulmont.shamrock.as.google.places.gate.dto.Place;
import com.haulmont.shamrock.as.google.places.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.places.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.places.gate.services.GoogleGeocodingService;
import com.haulmont.shamrock.as.google.places.gate.services.GooglePlacesService;
import com.haulmont.shamrock.as.google.places.gate.utils.GoogleAddressUtils;
import com.haulmont.shamrock.as.utils.AddressHelper;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PlaceDetailsService {

    private final AddressComponentsParser_EN componentsParser = new AddressComponentsParser_EN();

    @Inject
    private Logger logger;

    @Inject
    private GooglePlacesService googlePlacesService;

    @Inject
    private GoogleGeocodingService googleGeocodingService;

    @Inject
    private PlaceDetailsConverterService placeDetailsConverterService;

    @Inject
    private GeocodeCache cache;

    //
    @Inject
    private Configuration configuration;

    //

    public Address getDetails(RefineContext ctx) {
        if (ctx.getAddress().isRefined()) {
            return AddressHelper.convert(ctx.getAddress(), ctx.getRefineType());
        } else {
            try {
                Address a = ctx.getAddress();

                String id = AddressHelper.getAddressId(a);
                if (id == null) return null;

                AddressData data = a.getAddressData();
                String formattedAddress = data == null ? null : data.getFormattedAddress();
                ctx.setPlaceId(id);
                ctx.setFormattedAddress(formattedAddress);
                ctx.setPreferGeocoding(Optional.ofNullable(configuration.getUseGeocodeAPIForPlaceDetails()).orElse(Boolean.FALSE));

                return cache.getOrLookupOne(ctx, Converters::forRefine, this::getPlaceDetails);
            } catch (ServiceException e) {
                throw e;
            } catch (Throwable t) {
                throw new ServiceException(ErrorCode.SERVER_ERROR, "Unknown error", t);
            }
        }
    }

    private Address getPlaceDetails(RefineContext context){
        PlaceDetails placeDetails = null;
        if (StringUtils.isNotBlank(context.getFormattedAddress()) && context.isPreferGeocoding())
            placeDetails = getPlaceDetailsViaGeocoding(context.getFormattedAddress(), context.getPlaceId());
        if (placeDetails == null)
            placeDetails = googlePlacesService.getPlaceDetails(context.getPlaceId());
        return asAddress(placeDetails, context);
    }

    private PlaceDetails getPlaceDetailsViaGeocoding(String formattedAddress, String id) {
        PlaceDetails placeDetails = googleGeocodingService.getPlaceDetails(id);
        if (placeDetails != null) {
            String[] parts = formattedAddress.split(", ");
            String name = parts[0];

            List<String> types = placeDetails.getTypes();
            if (!GoogleAddressUtils.isBuilding(types)) {
                if (isBuilding(name)) {
                    Map<String, AddressComponent> components = GoogleAddressUtils.convert(placeDetails.getAddressComponents());
                    String buildingName = GoogleAddressUtils.getFirstLong(components, GElement.premise, GElement.subpremise);

                    if (StringUtils.isBlank(buildingName)) {
                        AddressComponent component = new AddressComponent();

                        component.setTypes(Collections.singletonList(GElement.premise.name()));
                        component.setLongText(name);
                        component.setShortText(name);

                        placeDetails.getAddressComponents().add(component);
                    }
                } else {
                    placeDetails.setDisplayName(new Place.DisplayName(name));
                }
            }
        }
        return placeDetails;
    }

    private Address asAddress(PlaceDetails placeDetails, com.haulmont.shamrock.as.contexts.RefineContext ctx) {
        AddressData data = ctx.getAddress().getAddressData();
        String formattedAddress = data == null ? null : data.getFormattedAddress();

        if (placeDetails == null) {
            return null;
        } else {
            Address address = placeDetailsConverterService.convert(placeDetails);
            if (address != null) {
                AddressData addressData = ctx.getAddress().getAddressData();
                String country = addressData != null && addressData.getAddressComponents() != null ? addressData.getAddressComponents().getCountry() : "N/A";
                logger.info(
                        String.format(
                                "Refine address '%s/%s' (%s, %s), result: %s",
                                ctx.getAddress().getId(), formattedAddress,
                                ctx.getRefineType().name(), country,
                                address.getAddressData().getFormattedAddress()
                        )
                );
            }

            return address;
        }
    }

    private boolean isBuilding(String name) {
        try {
            return componentsParser.parseBuildingName(new String[]{name}) != null;
        } catch (Exception e) {
            logger.warn("Fail to parse building name", e);
            return false;
        }
    }

}