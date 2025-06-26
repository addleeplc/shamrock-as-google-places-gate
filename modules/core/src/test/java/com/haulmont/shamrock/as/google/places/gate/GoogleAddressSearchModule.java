/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate;

import com.haulmont.monaco.App;
import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.Version;
import com.haulmont.monaco.annotations.Module;
import com.haulmont.monaco.container.ModuleLoader;
import com.haulmont.shamrock.as.google.places.gate.config.GoogleConfigurationStorage;
import com.haulmont.shamrock.as.google.places.gate.config.MockGoogleConfigurationService;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Date;

@Module(name = "google-address-search-module", depends = {"monaco-core"})
public class GoogleAddressSearchModule extends ModuleLoader {
    public GoogleAddressSearchModule() {
        AppContext.setProperty("jetty.port", 9999);

        Field field = FieldUtils.getDeclaredField(App.class, "version", true);
        try {
            FieldUtils.writeStaticField(field, new Version("1.3", "master", new Date()));
        } catch (IllegalAccessException e) {
            LoggerFactory.getLogger(GoogleAddressSearchModule.class).warn("Fail to set app version", e);
        }

        //

        component(GoogleConfigurationStorage.class);
        component(MockGoogleConfigurationService.class);
    }
}
