/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate;

import com.haulmont.monaco.annotations.Module;
import com.haulmont.monaco.container.ModuleLoader;
import com.haulmont.shamrock.as.google.gate.parser.Parser;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@Module(name = "google-address-search-module", depends = {"monaco-core"})
public class GoogleAddressSearchModule extends ModuleLoader {
    private static Logger logger = LoggerFactory.getLogger(GoogleAddressSearchModule.class);

    public GoogleAddressSearchModule() {
        super();
        packages(getClass().getPackage().getName());

        final String baseParsersPkg = "com.haulmont.shamrock.as.google.gate.parser";
        Reflections reflections = new Reflections(baseParsersPkg);

        final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Parser.class);
        for (Class<?> aClass : classes) {
            final Parser annotation = aClass.getAnnotation(Parser.class);
            try {
                if (StringUtils.isBlank(annotation.value())) {
                    component(baseParsersPkg, aClass.newInstance());
                } else {
                    component(aClass.getPackage().getName(), aClass.newInstance());
                }
            } catch (Throwable t) {
                logger.error("Unable to init parser " + (StringUtils.isBlank(annotation.value()) ? "Default" : annotation.value()), t);
                throw new RuntimeException(t);
            }
        }
    }
}
