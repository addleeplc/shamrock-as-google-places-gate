/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate;

import com.haulmont.monaco.config.annotations.Config;
import com.haulmont.monaco.config.annotations.Property;
import org.picocontainer.annotations.Component;

@Config
@Component
public interface ServiceConfiguration {

    String ENABLE_FORMATTED_ADDRESS_PARSING = "enableFormattedAddressParsing";
    String FILTER_NON_PARSED_ADDRESSES = "filterNonParsedAddressed";
    String CALL_DETAILS_FOR_NON_PARSED_ADDRESSES = "callDetailsForNonParsedAddressed";
    String USE_GEOCODE_API_FOR_PLACE_DETAILS = "useGeocodeAPIForPlaceDetails";

    //

    @Property("countries.postcode.require")
    String getCountriesRequirePostcode();

    @Property(ENABLE_FORMATTED_ADDRESS_PARSING)
    Boolean getEnableFormattedAddressParsing();

    @Property(FILTER_NON_PARSED_ADDRESSES)
    Boolean geFilterNonParsedAddressed();

    @Property(CALL_DETAILS_FOR_NON_PARSED_ADDRESSES)
    Boolean getCallDetailsForNonParsedAddressed();

    @Property(USE_GEOCODE_API_FOR_PLACE_DETAILS)
    Boolean getUseGeocodeAPIForPlaceDetails();

    @Property("filters.airports.enabled")
    Boolean getFilterAirports();

}