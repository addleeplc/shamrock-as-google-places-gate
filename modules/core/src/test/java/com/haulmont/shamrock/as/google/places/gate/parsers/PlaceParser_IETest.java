/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.parsers;

import com.haulmont.shamrock.as.dto.AddressComponents;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PlaceParser_IETest extends AbstractPlaceParserTest {

    @Test
    public void test() {
        PlaceParser parser = getParser();

        AddressComponents components;

        //

        components = parser.parse(createPlace("Armstrong Machinery LTD, Jordanstown, Lusk, County Dublin, Ireland"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "IE");
        Assert.assertNull(components.getPostcode());
        Assert.assertEquals(components.getCity(), "Lusk");
        Assert.assertEquals(components.getAddress(), "Armstrong Machinery LTD, Jordanstown");

        //

        components = parser.parse(createPlace("72 South Great George's Street, Dublin, D02 EC94, Ireland"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "IE");
        Assert.assertEquals(components.getPostcode(), "D02 EC94");
        Assert.assertEquals(components.getCity(), "Dublin");
        Assert.assertEquals(components.getAddress(), "72 South Great George's Street");

    }

    @Override
    protected PlaceParser getParser() {
        return new PlaceParser_IE();
    }

    @Test
    public void testAutocomplete() {
        PlaceParser parser = getParser();

        AddressComponents components;

        //

        components = parser.parse(createPlace("Armstrong Machinery The Five Roads, Jordanstown, Lusk, Co. Dublin, Ireland"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "IE");
        Assert.assertNull(components.getPostcode());
        Assert.assertEquals(components.getCity(), "Lusk");
        Assert.assertEquals(components.getAddress(), "Armstrong Machinery The Five Roads, Jordanstown");

        //

        components = parser.parse(createPlace("Yamamori South City, South Great George's Street, Dublin, Ireland"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "IE");
        Assert.assertNull(components.getPostcode());
        Assert.assertEquals(components.getCity(), "Dublin");
        Assert.assertEquals(components.getAddress(), "Yamamori South City, South Great George's Street");

    }

}