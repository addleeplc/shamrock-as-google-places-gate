/*
 * Copyright
 */

package com.haulmont.shamrock.as.google.gate;

import com.haulmont.shamrock.as.context.AutocompleteContext;
import com.haulmont.shamrock.as.contexts.GeocodeContext;
import com.haulmont.shamrock.as.contexts.RefineContext;
import com.haulmont.shamrock.as.contexts.ReverseGeocodingContext;
import com.haulmont.shamrock.as.contexts.SearchContext;
import com.haulmont.shamrock.as.dto.Address;

import java.util.List;

public interface AddressSearchGate {
    String getId();
    List<Address> search(SearchContext context);
    List<Address> autocomplete(AutocompleteContext ctx);

    Address geocode(GeocodeContext context);
    List<Address> reverseGeocode(ReverseGeocodingContext context);

    Address refine(RefineContext context);
}
