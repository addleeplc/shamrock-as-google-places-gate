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

    @Property("api.url")
    String getApiUrl();

    @Property("api.key.places")
    String getGooglePlacesApiKey();

    @Property("api.key.geocode")
    String getGoogleGeocodeApiKey();

    @Property("countries.postcode.require")
    String getCountriesRequirePostcode();

    @Property(ENABLE_FORMATTED_ADDRESS_PARSING)
    Boolean getEnableFormattedAddressParsing();
}
