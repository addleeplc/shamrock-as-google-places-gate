/*
 * Copyright
 */

package com.haulmont.shamrock.as.google.gate.parsers;

import com.haulmont.shamrock.as.google.gate.dto.Place;

public abstract class AbstractPlaceParserTest {
    protected abstract PlaceParser getParser();

    protected Place createPlace(String formattedAddress) {
        Place res = new Place();

        res.setFormattedAddress(formattedAddress);

        return res;
    }
}
