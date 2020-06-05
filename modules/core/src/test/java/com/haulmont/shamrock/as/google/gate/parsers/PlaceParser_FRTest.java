/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parsers;

import com.haulmont.shamrock.as.dto.AddressComponents;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PlaceParser_FRTest extends AbstractPlaceParserTest {

    @Test
    public void test() {
        PlaceParser parser = getParser();

        AddressComponents components;

        //

        components = parser.parse(createPlace("62 Rue Germain Defresne, 94400 Vitry-sur-Seine, France"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "FR");
        Assert.assertEquals(components.getPostcode(), "94400");
        Assert.assertEquals(components.getCity(), "Vitry-sur-Seine");
        Assert.assertEquals(components.getAddress(), "62 Rue Germain Defresne");

        //


    }

    @Override
    protected PlaceParser getParser() {
        return new PlaceParser_FR();
    }

    @Test
    public void testAutocomplete() {
        PlaceParser parser = getParser();

        AddressComponents components;

        //

        components = parser.parse(createPlace("Johann Raclot, Rue Germain Defresne, Vitry-sur-Seine, France"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "FR");
        Assert.assertNull(components.getPostcode());
        Assert.assertEquals(components.getCity(), "Vitry-sur-Seine");
        Assert.assertEquals(components.getAddress(), "Johann Raclot, Rue Germain Defresne");

    }

}