/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate;

import com.haulmont.monaco.ServiceException;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.shamrock.address.Address;
import com.haulmont.shamrock.address.AddressComponents;
import com.haulmont.shamrock.address.GeocodeContext;
import com.haulmont.shamrock.address.Location;
import com.haulmont.shamrock.address.context.RefineContext;
import com.haulmont.shamrock.address.context.ReverseGeocodingContext;
import com.haulmont.shamrock.address.context.SearchBeneathContext;
import com.haulmont.shamrock.address.context.SearchContext;
import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.as.context.AutocompleteContext;
import com.haulmont.shamrock.as.google.gate.converters.PlaceDetailsConverterService;
import com.haulmont.shamrock.as.google.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.gate.services.GoogleGeocodingService;
import com.haulmont.shamrock.as.google.gate.services.GooglePlacesService;
import com.haulmont.shamrock.geo.PostcodeHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleGeocodeAddressSearchGate implements AddressSearchGate {

    @Inject
    private Logger logger;

    @Inject
    private GoogleGeocodingService googleGeocodingService;

    @Inject
    private GooglePlacesService googlePlacesService;

    @Inject
    private PlaceDetailsConverterService placeDetailsConverterService;


    @Override
    public String getId() {
        return "google-geocode";
    }

    public List<Address> searchBeneath(SearchBeneathContext context) {
        throw new UnsupportedOperationException("Unsupported for " + getId() + " gate");
    }

    @Override
    public List<Address> search(SearchContext context) {
        long ts = System.currentTimeMillis();

        final List<Address> res = new ArrayList<>();

        List<PlaceDetails> places = googleGeocodingService.geocode(context);

        if (CollectionUtils.isNotEmpty(places)) res.addAll(convertSearchResponse(places));

        logger.debug("Search address by text (text: '{}', resSize: {}) ({} ms)'", context.getSearchString(), res.size(), System.currentTimeMillis() - ts);

        return res;
    }

    @Override
    public List<Address> autocomplete(AutocompleteContext ctx) {
        throw new UnsupportedOperationException("Unsupported operation for " + getId() + " gate");
    }

    private List<Address> convertSearchResponse(List<PlaceDetails> places) {
        List<Address> res = new ArrayList<>();

        for (PlaceDetails place : places) {
            try {
                Address address = placeDetailsConverterService.convert(place, getId());
                if (address != null) {
                    res.add(address);
                }
            } catch (Throwable e) {
                logger.warn("Failed to parse address", e);
            }
        }

        return res;
    }

    @Override
    public Address refine(RefineContext context) {
        return doRefine(context);
    }

    @Override
    public Address geocode(GeocodeContext context) {
        long ts = System.currentTimeMillis();

        Location loc = context.getLocation();
        if (loc != null && loc.getLat() != null && loc.getLon() != null) {
            final Address res = geocodeByLocation(context);

            logger.debug("Geocode address by location (loc: {},{}, res: '{}') ({} ms)", loc.getLat(), loc.getLon(), res != null ? res.getAddressData().getFormattedAddress() : "N/A", System.currentTimeMillis() - ts);

            return res;
        } else {
            final Address res = geocodeByAddress(context);

            logger.debug("Geocode address by text (text: '{}', res: '{}') ({} ms)", context.getAddress(), res != null ? res.getAddressData().getFormattedAddress() : "N/A", System.currentTimeMillis() - ts);

            return res;
        }
    }

    private Address geocodeByAddress(final GeocodeContext context) {
        List<PlaceDetails> places = googleGeocodingService.geocode(context);

        if (CollectionUtils.isNotEmpty(places)) {
            Address address = convertGeocodeResponse(places);

            if (address != null) {
                AddressComponents addressComponents = address.getAddressData().getAddressComponents();
                if (StringUtils.isNotBlank(addressComponents.getPostcode())) {
                    if (StringUtils.equalsIgnoreCase(context.getCountry(), "GB") || StringUtils.equalsIgnoreCase(addressComponents.getCountry(), "GB")) {
                        String postcode = context.getPostcode();
                        if (StringUtils.isBlank(postcode))
                            postcode = PostcodeHelper.parsePostcode(context.getAddress());

                        if (StringUtils.isNotBlank(postcode) && StringUtils.isNotBlank(addressComponents.getPostcode())) {
                            String postcodeArea = postcode.substring(0, postcode.length() - 2);

                            if (!StringUtils.startsWithIgnoreCase(addressComponents.getPostcode().replace(" ", ""), postcodeArea.replace(" ", "")))
                                return null;
                        }
                    }
                }
            }

            return address;
        }

        return null;
    }

    private Address geocodeByLocation(final GeocodeContext context) {
        List<PlaceDetails> places = googleGeocodingService.reverseGeocode(context);

        if (CollectionUtils.isNotEmpty(places)) {
            return convertGeocodeResponse(places);
        }

        return null;
    }

    @Override
    public List<Address> reverseGeocode(ReverseGeocodingContext context) {
        throw new UnsupportedOperationException("Unsupported operation for " + getId() + " gate");
    }

    private Address convertGeocodeResponse(List<PlaceDetails> places) {
        for (PlaceDetails place : places) {
            try {
                Address address = placeDetailsConverterService.convert(place, getId());
                if (address != null) {
                    return address;
                }
            } catch (Throwable e) {
                logger.warn("Failed to parse address", e);
            }
        }

        return null;
    }

    private Address doRefine(RefineContext ctx) {
        if (ctx.getAddress().isRefined()) {
            return AddressHelper.convert(ctx.getAddress(), ctx.getRefineType());
        } else {
            try {
                Address a = ctx.getAddress();

                String id = AddressHelper.getAddressId(a);
                if (id == null) return null;

                PlaceDetails placeDetails = googlePlacesService.getPlaceDetails(id);

                if (placeDetails == null) {
                    return null;
                } else {
                    Address address = convertRefineResult(placeDetails);
                    if (address != null) {
                        logger.info(
                                String.format(
                                        "Refine address '%s/%s' (%s, %s), result: %s",
                                        a.getId(), a.getAddressData().getFormattedAddress(),
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

    private Address convertRefineResult(PlaceDetails place) {
        return placeDetailsConverterService.convert(place, getId());
    }

}
