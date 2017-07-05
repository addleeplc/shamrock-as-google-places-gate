/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate;

import com.haulmont.monaco.AppContext;
import com.haulmont.shamrock.address.Address;
import com.haulmont.shamrock.address.AddressSearchGate;
import com.haulmont.shamrock.address.GeocodeContext;
import com.haulmont.shamrock.address.context.RefineContext;
import com.haulmont.shamrock.address.context.ReverseGeocodingContext;
import com.haulmont.shamrock.address.context.SearchBeneathContext;
import com.haulmont.shamrock.address.context.SearchContext;

import java.util.List;

/**
 * Created by Nikita Bozhko on 02.01.17.
 * Project Shamrock
 */
public class GoogleAddressSearchGate implements AddressSearchGate {

    private final AddressSearchGate delegator;

    public GoogleAddressSearchGate() {
        if (AppContext.getServiceName().contains("google-places")) {
            delegator = new GooglePlacesAddressSearchGate();
        } else if (AppContext.getServiceName().contains("google-geocode")) {
            delegator = new GoogleGeocodeAddressSearchGate();
        } else {
            delegator = null;
        }
    }

    @Override
    public String getId() {
        return delegator.getId();
    }

    @Override
    public List<Address> search(SearchContext context) {
        return delegator.search(context);
    }

    @Override
    public List<Address> searchBeneath(SearchBeneathContext context) {
        return delegator.searchBeneath(context);
    }

    @Override
    public Address refine(RefineContext context) {
        return delegator.refine(context);
    }

    @Override
    public Address geocode(GeocodeContext context) {
        return delegator.geocode(context);
    }

    @Override
    public List<Address> reverseGeocode(ReverseGeocodingContext context) {
        return delegator.reverseGeocode(context);
    }
}
