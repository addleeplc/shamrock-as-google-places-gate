/*
 * Copyright
 */

package com.haulmont.shamrock.as.google.gate;

import com.haulmont.monaco.ServiceException;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.shamrock.as.commons.parsers.AddressComponentsParser_EN;
import com.haulmont.shamrock.as.contexts.RefineContext;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.dto.AddressData;
import com.haulmont.shamrock.as.google.gate.converters.PlaceDetailsConverterService;
import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.services.GoogleGeocodingService;
import com.haulmont.shamrock.as.google.gate.services.GooglePlacesService;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressUtils;
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

    @Inject
    private Logger logger;

    @Inject
    private GooglePlacesService googlePlacesService;

    @Inject
    private GoogleGeocodingService googleGeocodingService;

    @Inject
    private PlaceDetailsConverterService placeDetailsConverterService;

    @Inject
    private ServiceConfiguration configuration;

    //

    private AddressComponentsParser_EN componentsParser = new AddressComponentsParser_EN();

    //

    public Address getDetails(RefineContext ctx, String source) {
        if (ctx.getAddress().isRefined()) {
            return AddressHelper.convert(ctx.getAddress(), ctx.getRefineType());
        } else {
            try {
                Address a = ctx.getAddress();

                String id = AddressHelper.getAddressId(a);
                if (id == null) return null;

                AddressData data = a.getAddressData();
                String formattedAddress = data == null ? null : data.getFormattedAddress();

                PlaceDetails placeDetails;

                if (StringUtils.isNotBlank(formattedAddress) &&
                        Optional.ofNullable(configuration.getUseGeocodeAPIForPlaceDetails()).orElse(Boolean.FALSE)
                ) {
                    placeDetails = googleGeocodingService.getPlaceDetails(id);
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
                                    component.setLongName(name);
                                    component.setShortName(name);

                                    placeDetails.getAddressComponents().add(component);
                                }
                            } else {
                                placeDetails.setName(name);
                            }
                        }
                    } else {
                        placeDetails = googlePlacesService.getPlaceDetails(id);
                    }
                } else {
                    placeDetails = googlePlacesService.getPlaceDetails(id);
                }

                if (placeDetails == null) {
                    return null;
                } else {
                    Address address = placeDetailsConverterService.convert(placeDetails, source);
                    if (address != null) {
                        AddressData addressData = ctx.getAddress().getAddressData();
                        String country = addressData != null && addressData.getAddressComponents() != null ? addressData.getAddressComponents().getCountry() : "N/A";
                        logger.info(
                                String.format(
                                        "Refine address '%s/%s' (%s, %s), result: %s",
                                        a.getId(), formattedAddress,
                                        ctx.getRefineType().name(), country,
                                        address.getAddressData().getFormattedAddress()
                                )
                        );
                    }

                    return address;
                }
            } catch (ServiceException e) {
                throw e;
            } catch (Throwable t) {
                throw new ServiceException(ErrorCode.SERVER_ERROR, "Unknown error", t);
            }
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
