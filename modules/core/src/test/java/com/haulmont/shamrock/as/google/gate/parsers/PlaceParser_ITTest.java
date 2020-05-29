/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parsers;

import com.haulmont.shamrock.address.AddressComponents;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PlaceParser_ITTest extends AbstractPlaceParserTest {

    @Test
    public void test() {
        PlaceParser parser = getParser();

        AddressComponents components;

        //

        components = parser.parse(createPlace("Viale Circe, 228, 04019 Terracina LT, Italy"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "IT");
        Assert.assertEquals(components.getPostcode(), "04019");
        Assert.assertEquals(components.getCity(), "Terracina");
        Assert.assertEquals(components.getAddress(), "Viale Circe, 228");

        //


    }

    @Override
    protected PlaceParser getParser() {
        return new PlaceParser_IT();
    }

    @Test
    public void testAutocomplete() {
        PlaceParser parser = getParser();

        AddressComponents components;

        //

        components = parser.parse(createPlace("Stabilimento La Stiva, Viale Circe, Terracina, Province of Latina, Italy"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "IT");
        Assert.assertNull(components.getPostcode());
        Assert.assertEquals(components.getCity(), "Terracina");
        Assert.assertEquals(components.getAddress(), "Stabilimento La Stiva, Viale Circe");

    }

}