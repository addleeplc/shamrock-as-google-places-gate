/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate;

import com.haulmont.shamrock.as.context.AutocompleteContext;
import com.haulmont.shamrock.as.contexts.GeoRegion;
import com.haulmont.shamrock.as.contexts.SearchContext;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.google.places.gate.cache.SearchResCache;
import com.haulmont.shamrock.as.google.places.gate.cache.SearchResKey;
import com.haulmont.shamrock.as.google.places.gate.cache.SearchResKeyBuilder;
import com.haulmont.shamrock.as.google.places.gate.dto.RefineContext;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;

import java.util.Collections;
import java.util.List;

@Component
public class AddressSearchService {

    @Inject
    private SearchByTextService searchByTextService;

    @Inject
    private AutocompleteService autocompleteService;

    @Inject
    private SearchNearbyService searchNearbyService;

    @Inject
    private PlaceDetailsService placeDetailsService;

    @Inject
    private SearchResCache cache;

    //

    public List<Address> search(final SearchContext context) {
        return cache.get(context, new SearchResCache.SearchResKeyLoader<>() {

            @Override
            public SearchResKey getKey(SearchContext context) {
                return SearchResKeyBuilder.buildFrom(context);
            }

            @Override
            public List<Address> getValue(SearchContext context) {
                return searchByTextService.search(context);
            }
        });
    }

    public List<Address> autocomplete(AutocompleteContext context) {
        return cache.get(context, new SearchResCache.SearchResKeyLoader<>() {

            @Override
            public SearchResKey getKey(AutocompleteContext context) {
                return SearchResKeyBuilder.buildFrom(context);
            }

            @Override
            public List<Address> getValue(AutocompleteContext context) {
                return autocompleteService.autocomplete(context);
            }
        });
    }

    public List<Address> searchNearby(GeoRegion context) {
        return cache.get(context, new SearchResCache.SearchResKeyLoader<>() {

            @Override
            public SearchResKey getKey(GeoRegion context) {
                return SearchResKeyBuilder.buildFrom(context);
            }

            @Override
            public List<Address> getValue(GeoRegion context) {
                return searchNearbyService.searchNearby(context);
            }
        });
    }

    public Address refine(RefineContext context) {
        List<Address> addresses = cache.get(context, new SearchResCache.SearchResKeyLoader<>() {
            @Override
            public SearchResKey getKey(RefineContext context) {
                return SearchResKeyBuilder.buildFrom(context);
            }

            @Override
            public List<Address> getValue(RefineContext context) {
                return wrap(placeDetailsService.getDetails(context));
            }
        });
        return first(addresses);
    }

    //

    private static Address first(List<Address> addresses) {
        return addresses.isEmpty() ? null : addresses.get(0);
    }

    private static List<Address> wrap(Address address) {
        return address == null ? Collections.emptyList() : Collections.singletonList(address);
    }

}