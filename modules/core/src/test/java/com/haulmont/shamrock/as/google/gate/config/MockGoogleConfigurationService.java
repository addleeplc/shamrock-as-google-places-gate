/*
 * Copyright 2008 - 2021 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.config;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class MockGoogleConfigurationService extends GoogleConfigurationService {

    private static final Map<String, String> PROPERTIES = ImmutableMap.of(
            "tbs", "101",
            "cs", "102",
            "sherlock_*_integration", "999"
    );

    @Override
    protected Map<String, String> getProperties() {
        return PROPERTIES;
    }
}
