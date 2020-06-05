/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parsers;

import com.haulmont.shamrock.as.dto.AddressComponents;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PlaceParser_DETest extends AbstractPlaceParserTest {

    @Test
    public void test() {
        PlaceParser parser = getParser();

        AddressComponents components;

        //

        components = parser.parse(createPlace("Attilastraße 177, 12105 Berlin, Germany"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "DE");
        Assert.assertEquals(components.getPostcode(), "12105");
        Assert.assertEquals(components.getCity(), "Berlin");
        Assert.assertEquals(components.getAddress(), "Attilastraße 177");

        //


    }

    @Override
    protected PlaceParser getParser() {
        return new PlaceParser_DE();
    }

    @Test
    public void testAutocomplete() {
        PlaceParser parser = getParser();

        AddressComponents components;

        //

        components = parser.parse(createPlace("Platz des 4. Juli, Berlin, Germany"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "DE");
        Assert.assertNull(components.getPostcode());
        Assert.assertEquals(components.getCity(), "Berlin");
        Assert.assertEquals(components.getAddress(), "Platz des 4. Juli");

    }

}