/*
 * Copyright
 */

package com.haulmont.shamrock.as.google.gate;

import com.haulmont.monaco.ServiceException;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.shamrock.address.Address;
import com.haulmont.shamrock.address.AddressData;
import com.haulmont.shamrock.address.context.RefineContext;
import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.as.google.gate.converters.PlaceDetailsConverterService;
import com.haulmont.shamrock.as.google.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.gate.services.GoogleGeocodingService;
import com.haulmont.shamrock.as.google.gate.services.GooglePlacesService;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;

import java.util.List;
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

    public Address getDetails(RefineContext ctx, String source) {
        if (ctx.getAddress().isRefined()) {
            return AddressHelper.convert(ctx.getAddress(), ctx.getRefineType());
        } else {
            try {
                Address a = ctx.getAddress();

                String id = AddressHelper.getAddressId(a);
                if (id == null) return null;

                AddressData data = a.getAddressData();
                String formattedAddress = data.getFormattedAddress();

                PlaceDetails placeDetails;

                if (Optional.ofNullable(configuration.getUseGeocodeAPIForPlaceDetails()).orElse(Boolean.FALSE)) {
                    placeDetails = googleGeocodingService.getPlaceDetails(id);
                    if (placeDetails != null) {

                        String[] parts = formattedAddress.split(", ");
                        String name = parts[0];

                        List<String> types = placeDetails.getTypes();
                        if (!GoogleAddressUtils.isBuilding(types)
                        ) {
                            placeDetails.setName(name);
                        }
                    }
                } else {
                    placeDetails = googlePlacesService.getPlaceDetails(id);
                }

                if (placeDetails == null) {
                    return null;
                } else {
                    Address address = convertRefineResult(placeDetails, source);
                    if (address != null) {
                        logger.info(
                                String.format(
                                        "Refine address '%s/%s' (%s, %s), result: %s",
                                        a.getId(), formattedAddress,
                                        ctx.getRefineType().name(), ctx.getAddress().getAddressData().getAddressComponents().getCountry(),
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

    private Address convertRefineResult(PlaceDetails place, String source) {
        return placeDetailsConverterService.convert(place, source);
    }
}
