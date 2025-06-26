/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate;

import com.google.common.collect.Lists;
import com.haulmont.monaco.ServiceException;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.shamrock.as.context.AutocompleteContext;
import com.haulmont.shamrock.as.contexts.RefineContext;
import com.haulmont.shamrock.as.contexts.RefineType;
import com.haulmont.shamrock.as.contexts.ReverseGeocodingContext;
import com.haulmont.shamrock.as.contexts.SearchContext;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.dto.AddressComponents;
import com.haulmont.shamrock.as.dto.AddressData;
import com.haulmont.shamrock.as.google.places.gate.converters.PlaceDetailsConverterService;
import com.haulmont.shamrock.as.google.places.gate.dto.Place;
import com.haulmont.shamrock.as.google.places.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.places.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.places.gate.parsers.PlaceParsingService;
import com.haulmont.shamrock.as.google.places.gate.services.GooglePlacesService;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.LatLng;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.PlacePrediction;
import com.haulmont.shamrock.as.google.places.gate.utils.GoogleAddressSearchUtils;
import com.haulmont.shamrock.as.google.places.gate.utils.GoogleAddressUtils;
import com.haulmont.shamrock.as.utils.GeoHelper;
import com.haulmont.shamrock.geo.PostcodeHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;

import java.util.*;

@Component
public class GooglePlacesAddressSearchGate {

    @Inject
    private Logger logger;

    @Inject
    private GooglePlacesService googlePlacesService;

    @Inject
    private PlaceParsingService placeParsingService;

    @Inject
    private PlaceDetailsService placeDetailsService;

    @Inject
    private PlaceDetailsConverterService placeDetailsConverterService;

    @Inject
    private Configuration configuration;

    //

    public GooglePlacesAddressSearchGate() {

    }

    public List<Address> search(SearchContext context) {
        String searchString = context.getSearchString();
        if (StringUtils.isEmpty(searchString)) return null;

        long ts = System.currentTimeMillis();

        List<Address> addresses = new ArrayList<>();

        String postcode = PostcodeHelper.parsePostcode(context.getSearchString());
        if (postcode == null) postcode = context.getPostcode();

        boolean partialPostcode = PostcodeHelper.parsePostcode(postcode, false) == null;
        if (StringUtils.isNotBlank(postcode) && !partialPostcode && context.getSearchString().equalsIgnoreCase(postcode)) {
            addresses.addAll(doSearch(context));
        } else {
            if (StringUtils.isNotBlank(context.getCity())) {
                SearchContext temp = GoogleAddressSearchUtils.clone(context);
                temp.setCity(context.getCity());
                temp.setSearchString(searchString + ", " + context.getCity());

                addresses.addAll(doSearch(temp));
            } else {
                //First step
                addresses.addAll(doSearch(context));

                if (!haveGoodAddresses(context, addresses)) {
                    SearchContext temp = GoogleAddressSearchUtils.clone(context);
                    temp.setCity(context.getPreferredCity());
                    temp.setCountry(context.getPreferredCountry());
                    temp.setSearchString(searchString + ", " + context.getPreferredCity());

                    addresses.addAll(doSearch(temp));
                }
            }
        }

        final List<Address> res = GoogleAddressSearchUtils.filter(addresses);

        logger.debug("Search address by text (text: '{}', resSize: {}) ({} ms)'", context.getSearchString(), res.size(), System.currentTimeMillis() - ts);

        return res;
    }

    public List<Address> autocomplete(AutocompleteContext context) {
        long ts = System.currentTimeMillis();

        List<PlacePrediction> predictions = googlePlacesService.autocomplete(context);
        List<Address> res = convert(predictions);

        logger.debug("Autocomplete address (text: '{}', resSize: {}) ({} ms)'", context.getSearchString(), res.size(), System.currentTimeMillis() - ts);

        return res;
    }

    private List<Address> convert(List<PlacePrediction> predictions) {
        if (predictions == null) return Collections.emptyList();

        List<Address> res = new ArrayList<>();

        boolean filterNonParsedAddresses = Optional.ofNullable(configuration.geFilterNonParsedAddressed()).orElse(Boolean.TRUE);
        boolean callDetailsForNonParsedAddressed = Optional.ofNullable(configuration.getCallDetailsForNonParsedAddressed()).orElse(Boolean.FALSE);

        for (PlacePrediction prediction : predictions) {
            Address address = convert(prediction);

            if (filterNonParsedAddresses) {
                AddressData data = address.getAddressData();
                if (data != null) {
                    AddressComponents components = data.getAddressComponents();
                    if (components != null && StringUtils.isNotBlank(components.getCountry())) {
                        res.add(address);
                    } else if (callDetailsForNonParsedAddressed) {
                        res.add(refine(address));
                    }
                } else {
                    res.add(refine(address));
                }
            } else if (callDetailsForNonParsedAddressed) {
                res.add(address);
            }
        }

        return res;
    }

    public PlaceDetails getPlaceDetails(String placeId){
        return googlePlacesService.getPlaceDetails(placeId);
    }

    private Address convert(PlacePrediction prediction) {
        Place place = new Place();

        if (prediction.getText() != null)
            place.setFormattedAddress(prediction.getText().getText());
        place.setTypes(prediction.getTypes());
        place.setId(prediction.getPlaceId());

        Address address = placeParsingService.parse(place);
        if (address == null) {
            address = new Address();

            AddressData data = new AddressData();
            AddressComponents components = new AddressComponents();

            address.setId(String.format("google-places|%s", prediction.getPlaceId()));
            if (prediction.getText() != null)
                data.setFormattedAddress(prediction.getText().getText());

            data.setAddressComponents(components);
            address.setAddressData(data);
        }

        return address;
    }

    private boolean haveGoodAddresses(SearchContext context, Collection<Address> addresses) {
        if (CollectionUtils.isEmpty(addresses)) return false;

        boolean res = true;

        String preferredCountry = context.getPreferredCountry();
        if (StringUtils.isBlank(context.getCountry()) && StringUtils.isNotBlank(preferredCountry)) {
            for (Address address : addresses) {
                AddressData addressData = address.getAddressData();
                if (addressData != null) {
                    AddressComponents addressComponents = addressData.getAddressComponents();
                    if (addressComponents != null) res = res && StringUtils.equals(addressComponents.getCountry(), preferredCountry);
                }
            }
        }

        return res;
    }

    private List<Address> doSearch(final SearchContext context) {
        List<Place> places = new ArrayList<>();

        try {
            places = googlePlacesService.getPlaces(context);
        } catch (Throwable e) {
            logger.warn("Failed to search address", e);
        }

        if (CollectionUtils.isEmpty(places)) return Collections.emptyList();

        List<Address> res = new ArrayList<>();
        for (Place place : places) {
            if (!isValidPlaceResult(place)) continue;

            Address address = convert(context, place);
            if (address != null) res.add(address);
        }

        return res;
    }

    private boolean isValidPlaceResult(Place pr) {
        List<String> types = pr.getTypes();
        if (CollectionUtils.isEmpty(types)) return false;

        if (types.size() == 1) {
            return !org.apache.commons.collections4.CollectionUtils.containsAny(
                    types,
                    Arrays.asList(GElement.postal_code.name(), GElement.postal_town.name(), GElement.postal_code_prefix.name(), GElement.postal_code_suffix.name())
            );
        }

        return true;
    }

    private Address convert(SearchContext context, Place place) {
        try {
            Address res = convert(place);

            AddressData data = res.getAddressData();
            AddressComponents components = data.getAddressComponents();

            if (context.getCountry() != null) {
                components.setCountry(context.getCountry());
            }

            if (BooleanUtils.isTrue(configuration.getEnableFormattedAddressParsing())) {
                Address address = placeParsingService.parse(place);
                if (address == null) {
                    return refine(res);
                } else {
                    return address;
                }
            } else {
                return refine(res);
            }
        } catch (Throwable e) {
            logger.warn("Failed to parse address", e);
            return null;
        }
    }

    private Address refine(Address res) {
        RefineContext context = new RefineContext();
        context.setAddress(res);
        context.setRefineType(RefineType.DEFAULT);

        return refine(context);
    }


    private Address convert(Place place) {
        Address res = new Address();

        AddressData data = new AddressData();
        AddressComponents components = new AddressComponents();

        res.setId(String.format("google-places|%s", place.getId()));
        data.setFormattedAddress(GoogleAddressUtils.getFormattedAddress(place));

        data.setAddressComponents(components);
        res.setAddressData(data);

        return res;
    }

    public Address refine(RefineContext context) {
        return placeDetailsService.getDetails(context);
    }


    public List<Address> reverseGeocode(ReverseGeocodingContext context) {
        List<Address> res;

        long ts = System.currentTimeMillis();

        try {
            List<PlaceDetails> places = googlePlacesService.getPlaces(context);

            if (CollectionUtils.isEmpty(places))
                res = Collections.emptyList();
            else
                res = convertReverseGeocodeResults(context, places);

            String firstAddress;
            if (!CollectionUtils.isEmpty(res) && res.get(0) != null && res.get(0).getAddressData() != null)
                firstAddress = res.get(0).getAddressData().getFormattedAddress();
            else
                firstAddress = "N/A";

            logger.debug("Reverse geocode address by location (loc: {},{}, res: '{}', resSize: {}) ({} ms)",
                    context.getSearchRegion().getLatitude(), context.getSearchRegion().getLongitude(), firstAddress, res.size(), System.currentTimeMillis() - ts);

            return res;
        } catch (ServiceException e) {
            throw e;
        } catch (Throwable t) {
            throw new ServiceException(ErrorCode.SERVER_ERROR, "Unknown error", t);
        }
    }

    private List<Address> convertReverseGeocodeResults(ReverseGeocodingContext context, List<PlaceDetails> results) {
        List<Address> res = new ArrayList<>();
        for (PlaceDetails placeDetails : results) {
            try {
                if (placeDetails.getTypes().size() == 1 && placeDetails.getTypes().contains("route")) continue;

                Address address = placeDetailsConverterService.convert(placeDetails);
                address.getAddressData().setFormattedAddress(GoogleAddressUtils.getFormattedAddress(placeDetails));

                LatLng location = placeDetails.getLocation();
                if (location != null &&
                        location.getLatitude() != null && location.getLongitude() != null) {
                    double distance = GeoHelper.getGeoDistance(location.getLongitude(), location.getLatitude(), context.getSearchRegion().getLongitude(), context.getSearchRegion().getLatitude());
                    if (distance <= context.getSearchRegion().getRadius()) {
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
                        context.getSearchRegion().getLatitude(), context.getSearchRegion().getLongitude(),
                        context.getSearchRegion().getRadius(), res.size()
                )
        );

        return res;
    }
}