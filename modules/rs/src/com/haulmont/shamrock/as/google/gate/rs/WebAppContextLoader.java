/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.rs;

import com.haulmont.monaco.AppContextLoader;
import com.haulmont.shamrock.as.google.gate.GoogleAddressSearchGate;
import org.picocontainer.MutablePicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextListener;

/**
 * Created by Nikita Bozhko on 01.01.17.
 * Project Shamrock
 */

public class WebAppContextLoader extends AppContextLoader implements ServletContextListener {

    private static Logger log = LoggerFactory.getLogger(WebAppContextLoader.class);

    @Override
    protected void initContainer(MutablePicoContainer context) {
        context.addComponent(new GoogleAddressSearchGate());
    }
}
