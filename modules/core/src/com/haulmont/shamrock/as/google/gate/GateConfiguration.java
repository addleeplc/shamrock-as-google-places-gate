/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate;

import com.haulmont.monaco.config.annotations.Config;
import com.haulmont.monaco.config.annotations.Property;

@Config
public interface GateConfiguration {
    @Property("api.url")
    String getApiUrl();

    @Property("api.key.search")
    String getSearchApiKey();

    @Property("api.key.geocode")
    String getGeocodeApiKey();

    @Property("api.key.refine")
    String getRefineApiKey();

    @Property("api.key.reverseGeocode")
    String getReverseGeocodeApiKey();

    @Property("timeout")
    Integer getTimeout();

    @Property("distanceThreshold")
    Double getDistanceThreshold();
}
