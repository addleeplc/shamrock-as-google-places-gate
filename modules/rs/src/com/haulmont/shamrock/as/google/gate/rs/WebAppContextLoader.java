/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.rs;

import com.haulmont.monaco.AppContextLoader;
import com.haulmont.shamrock.as.google.gate.GoogleAddressSearchGate;
import com.haulmont.shamrock.as.google.gate.parser.Parser;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.MutablePicoContainer;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextListener;
import java.util.Set;

/**
 * Created by Nikita Bozhko on 01.01.17.
 * Project Shamrock
 */

public class WebAppContextLoader extends AppContextLoader implements ServletContextListener {

    private static Logger log = LoggerFactory.getLogger(WebAppContextLoader.class);

    @Override
    protected void initContainer(MutablePicoContainer context) {
        initParsers(context);

        context.addComponent(new GoogleAddressSearchGate());
    }

    private void initParsers(MutablePicoContainer context) {
        final String baseParsersPkg = "com.haulmont.shamrock.as.google.gate.parser";
        Reflections reflections = new Reflections(baseParsersPkg);

        final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Parser.class);
        for (Class<?> aClass : classes) {
            final Parser annotation = aClass.getAnnotation(Parser.class);
            try {
                if (StringUtils.isBlank(annotation.value())) {
                    context.addComponent(baseParsersPkg, aClass.newInstance());
                } else {
                    context.addComponent(aClass.getPackage().getName(), aClass.newInstance());
                }
            } catch (Throwable t) {
                log.error("Unable to init parser " + (StringUtils.isBlank(annotation.value()) ? "Default" : annotation.value()), t);
                throw new RuntimeException(t);
            }
        }
    }
}
