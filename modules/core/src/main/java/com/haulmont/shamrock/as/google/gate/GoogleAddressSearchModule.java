/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate;

import com.haulmont.monaco.annotations.Module;
import com.haulmont.monaco.container.ModuleLoader;
import com.haulmont.shamrock.as.google.gate.converters.PlaceDetailsConverter;
import com.haulmont.shamrock.as.google.gate.converters.PlaceDetailsConverterService;
import com.haulmont.shamrock.as.google.gate.parsers.PlaceParser;
import com.haulmont.shamrock.as.google.gate.parsers.PlaceParsingService;
import org.picocontainer.MutablePicoContainer;

@Module(name = "google-address-search-module", depends = {"monaco-core", "graylog-reporter", "sentry-reporter"})
public class GoogleAddressSearchModule extends ModuleLoader {

    public GoogleAddressSearchModule() {
        super();
        packages(getClass().getPackage().getName());

        register((container, c) -> {
            PlaceParser.Component a = c.getAnnotation(PlaceParser.Component.class);
            if (a != null) {
                container.addComponent(c);
                container.addComponent("google-address-search-ComponentLifecycleStrategy(" + c.getName() + ")", new PlaceParserLifecycleStrategy(container, c));

                return true;
            } else {
                return false;
            }
        });

        register((container, c) -> {
            PlaceDetailsConverter.Component a = c.getAnnotation(PlaceDetailsConverter.Component.class);
            if (a != null) {
                container.addComponent(c);
                container.addComponent("google-address-search-ComponentLifecycleStrategy(" + c.getName() + ")", new PlaceDetailsConverterLifecycleStrategy(container, c));

                return true;
            } else {
                return false;
            }
        });
    }

    public static class PlaceParserLifecycleStrategy {
        private MutablePicoContainer container;
        private Class<?> c;

        PlaceParserLifecycleStrategy(MutablePicoContainer container, Class<?> c) {
            this.container = container;
            this.c = c;
        }

        public void start() {
            PlaceParser.Component a = c.getAnnotation(PlaceParser.Component.class);
            PlaceParser parser = (PlaceParser) container.getComponent(c);

            PlaceParsingService service = container.getComponent(PlaceParsingService.class);
            service.register(parser);
        }

        public void stop() {

        }

        public void dispose() {

        }
    }

    public static class PlaceDetailsConverterLifecycleStrategy {
        private MutablePicoContainer container;
        private Class<?> c;

        PlaceDetailsConverterLifecycleStrategy(MutablePicoContainer container, Class<?> c) {
            this.container = container;
            this.c = c;
        }

        public void start() {
            PlaceDetailsConverter.Component a = c.getAnnotation(PlaceDetailsConverter.Component.class);
            PlaceDetailsConverter converter = (PlaceDetailsConverter) container.getComponent(c);

            PlaceDetailsConverterService service = container.getComponent(PlaceDetailsConverterService.class);
            service.register(a.country(), converter);
        }

        public void stop() {

        }

        public void dispose() {

        }
    }

}
