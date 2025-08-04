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

    @Property("countries.postcode.require")
    String getCountriesRequirePostcode();

    @Property("parsers.enabled")
    Boolean getEnableParsing();

    @Property("filterNonParsed")
    Boolean geFilterNonParsed();

    @Property("refineNonParsed")
    Boolean getRefineNonParsed();

    @Property("google.placeDetails.useGeocoding")
    Boolean getPlaceDetailsUseGeocoding();

    @Property("filters.airports.enabled")
    Boolean getFilterAirports();

}