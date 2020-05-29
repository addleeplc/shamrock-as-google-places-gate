/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parsers;

import com.haulmont.shamrock.address.AddressComponents;
import com.haulmont.shamrock.as.google.gate.dto.Place;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface PlaceParser {
    String COMPONENTS_DIVIDER = ", ";

    AddressComponents parse(Place place);

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @interface Component {
        String[] value();
    }
}
