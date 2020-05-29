/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parsers;

import com.haulmont.shamrock.address.AddressComponents;
import com.haulmont.shamrock.as.google.gate.dto.Place;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PlaceParser_USTest extends AbstractPlaceParserTest {

    @Test
    public void test() {
        PlaceParser parser = getParser();

        AddressComponents components;

        //

        components = parser.parse(createPlace("714 Washington St, Brookline, MA 02446, USA"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "US");
        Assert.assertEquals(components.getPostcode(), "02446");
        Assert.assertEquals(components.getCity(), "Brookline");
        Assert.assertEquals(components.getAddress(), "714 Washington St");
        Assert.assertEquals(components.getStreet(), "Washington St");

        //


    }

    @Override
    protected PlaceParser_US getParser() {
        return new PlaceParser_US();
    }

    @Test
    public void testAutocomplete() {
        PlaceParser parser = getParser();

        AddressComponents components;

        //

        components = parser.parse(createPlace("Washington Street, Brookline, MA, USA"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "US");
        Assert.assertNull(components.getPostcode());
        Assert.assertEquals(components.getCity(), "Brookline");
        Assert.assertEquals(components.getAddress(), "Washington Street");
        Assert.assertEquals(components.getStreet(), "Washington Street");

    }

}