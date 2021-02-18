/*
 * Copyright 2008 - 2021 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.config;

import com.haulmont.monaco.config.GlobalConfigurationPropertyStorage;
import org.picocontainer.annotations.Component;

@Component
public class GoogleConfigurationStorage extends GlobalConfigurationPropertyStorage {
    public GoogleConfigurationStorage() {
        super("google");
    }
}
