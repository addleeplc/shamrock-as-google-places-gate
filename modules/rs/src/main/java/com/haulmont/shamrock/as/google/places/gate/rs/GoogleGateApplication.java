/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.rs;

import com.haulmont.monaco.rs.jersey.Application;

import javax.ws.rs.ApplicationPath;

/**
 * Created by Nikita Bozhko on 01.01.17.
 * Project Shamrock
 */

@ApplicationPath("/")
public class GoogleGateApplication extends Application {
    public GoogleGateApplication() {
        super();
        packages(GoogleGateApplication.class.getPackage().getName());
    }
}
