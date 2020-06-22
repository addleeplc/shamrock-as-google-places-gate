/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate;

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

    @Property("api.url")
    String getApiUrl();

    //

    @Property("api.key.places")
    String getGooglePlacesApiKey();

    @Property("api.key.places.autocomplete")
    String getGooglePlacesAutocompleteApiKey();

    @Property("api.key.places.textSearch")
    String getGooglePlacesTextSearchApiKey();

    @Property("api.key.places.nearbySearch")
    String getGooglePlacesNearbySearchApiKey();

    @Property("api.key.places.details")
    String getGooglePlacesDetailsApiKey();

    //

    @Property("api.key.geocode")
    String getGoogleGeocodeApiKey();

    @Property("api.key.geocode.textSearch")
    String getGoogleGeocodeTextSearchApiKey();

    @Property("api.key.geocode.locationSearch")
    String getGoogleGeocodeLocationSearchApiKey();

    @Property("api.key.geocode.details")
    String getGoogleGeocodePlaceDetailsApiKey();

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

}
