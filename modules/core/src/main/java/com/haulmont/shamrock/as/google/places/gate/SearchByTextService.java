/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate;

import com.haulmont.shamrock.as.contexts.RefineType;
import com.haulmont.shamrock.as.contexts.SearchContext;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.dto.AddressComponents;
import com.haulmont.shamrock.as.dto.AddressData;
import com.haulmont.shamrock.as.google.places.gate.dto.Place;
import com.haulmont.shamrock.as.google.places.gate.dto.RefineContext;
import com.haulmont.shamrock.as.google.places.gate.parsers.PlaceParsingService;
import com.haulmont.shamrock.as.google.places.gate.services.GooglePlacesService;
import com.haulmont.shamrock.as.google.places.gate.utils.GoogleAddressSearchUtils;
import com.haulmont.shamrock.as.google.places.gate.utils.GoogleAddressUtils;
import com.haulmont.shamrock.geo.PostcodeHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SearchByTextService {

    @Inject
    private Logger logger;

    @Inject
    private GooglePlacesService googlePlacesService;

    @Inject
    private PlaceParsingService placeParsingService;

    @Inject
    private PlaceDetailsService placeDetailsService;

    @Inject
    private ServiceConfiguration configuration;

    //

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
                context.setSearchString(searchString + ", " + context.getCity());
                addresses.addAll(doSearch(context));
            } else {
                addresses.addAll(doSearch(context));

                if ((StringUtils.isBlank(context.getCountry()) && StringUtils.isNotBlank(context.getPreferredCountry())) ||
                        (StringUtils.isBlank(context.getCity()) && StringUtils.isNotBlank(context.getPreferredCity()))) {
                    if (!haveGoodAddress(context, addresses)) {
                        SearchContext temp = GoogleAddressSearchUtils.clone(context);
                        temp.setCity(context.getPreferredCity());
                        temp.setCountry(context.getPreferredCountry());
                        temp.setSearchString(searchString + ", " + context.getPreferredCity());

                        addresses.addAll(doSearch(temp));
                    }
                }
            }
        }

        final List<Address> res = GoogleAddressSearchUtils.filter(addresses);

        logger.debug("Search address by text (text: '{}', resSize: {}) ({} ms)'", context.getSearchString(), res.size(), System.currentTimeMillis() - ts);

        return res;
    }

    //

    private boolean haveGoodAddress(SearchContext context, Collection<Address> addresses) {
        if (CollectionUtils.isEmpty(addresses)) return false;

        String preferredCountry = GoogleAddressUtils.resolveRegionCode(StringUtils.isBlank(context.getCountry()) ? context.getPreferredCountry() : context.getCountry());
        String preferredCity = StringUtils.isBlank(context.getCity()) ? context.getPreferredCity() : context.getCity();

        for (Address address : addresses) {
            AddressData addressData = address.getAddressData();
            if (addressData != null) {
                AddressComponents addressComponents = addressData.getAddressComponents();
                if (addressComponents != null
                        && (StringUtils.isBlank(preferredCountry) || StringUtils.equals(addressComponents.getCountry(), preferredCountry))
                        && (StringUtils.isBlank(preferredCity) || StringUtils.equals(addressComponents.getCity(), preferredCity)))
                    return true;
            }
        }

        return false;
    }

    private List<Address> doSearch(final SearchContext context) {
        List<Place> places = new ArrayList<>();

        try {
            places = googlePlacesService.getPlaces(context);
        } catch (Throwable e) {
            logger.warn("Failed to search address", e);
        }

        if (CollectionUtils.isEmpty(places)) return Collections.emptyList();

        return places.stream()
                .filter(this::isValid)
                .map(place -> convert(context, place))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean isValid(Place place) {
        if (place == null) return false;

        List<String> types = place.getTypes();
        if (CollectionUtils.isEmpty(types)) return false;

        if (GoogleAddressUtils.isArea(types)) return false;
        if (isFilterAirports() && GoogleAddressUtils.isAirport(types)) return false;

        return true;
    }

    private Boolean isFilterAirports() {
        return Optional.ofNullable(configuration.getFilterAirports()).orElse(Boolean.TRUE);
    }

    //

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

    private Address refine(Address res) {
        RefineContext context = new RefineContext();
        context.setAddress(res);
        context.setRefineType(RefineType.DEFAULT);

        return refine(context);
    }


    public Address refine(RefineContext context) {
        return placeDetailsService.getDetails(context);
    }
}
