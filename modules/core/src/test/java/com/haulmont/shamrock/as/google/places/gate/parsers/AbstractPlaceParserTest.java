/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.parsers;

import com.haulmont.shamrock.as.google.places.gate.dto.Place;

public abstract class AbstractPlaceParserTest {
    protected abstract PlaceParser getParser();

    protected Place createPlace(String formattedAddress) {
        Place res = new Place();

        res.setFormattedAddress(formattedAddress);

        return res;
    }
}
