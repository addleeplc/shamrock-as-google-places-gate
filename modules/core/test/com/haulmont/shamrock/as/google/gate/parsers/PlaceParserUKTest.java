/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parsers;

import com.haulmont.shamrock.address.AddressComponents;
import com.haulmont.shamrock.as.google.gate.dto.Place;
import com.haulmont.shamrock.as.google.gate.parsers.PlaceParser_UK;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PlaceParserUKTest {

    @Test
    public void test() {
        PlaceParser_UK parser = new PlaceParser_UK();
        AddressComponents components;

        //

        components = parser.parse(createPlace("2 Duke of Wellington Ave, Woolwich, London SE18 6FR, UK"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getPostcode(), "SE18 6FR");
        Assert.assertEquals(components.getCity(), "London");
        Assert.assertEquals(components.getAddress(), "2 Duke of Wellington Ave");
        Assert.assertEquals(components.getStreet(), "Duke of Wellington Ave");

        //

        components = parser.parse(createPlace("133 Haling Park Rd, South Croydon CR2 6NN, UK"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getPostcode(), "CR2 6NN");
        Assert.assertEquals(components.getCity(), "South Croydon");
        Assert.assertEquals(components.getAddress(), "133 Haling Park Rd");
        Assert.assertEquals(components.getStreet(), "Haling Park Rd");


        //

//        components = parser.parse(createPlace("Guinness Road Trading Estate, Guinness Rd, Stretford, Manchester M17 1SB, UK"));
//
//        Assert.assertNotNull(components);
//        Assert.assertEquals(components.getPostcode(), "M17 1SB");
//        Assert.assertEquals(components.getCity(), "Stretford");
//        Assert.assertEquals(components.getAddress(), "Guinness Road Trading Estate, Guinness Rd");
//        Assert.assertEquals(components.getStreet(), "Guinness Rd");

        //

        components = parser.parse(createPlace("107 Scarle Rd, Wembley HA0 4SS, UK"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getPostcode(), "HA0 4SS");
        Assert.assertEquals(components.getCity(), "Wembley");
        Assert.assertEquals(components.getAddress(), "107 Scarle Rd");
        Assert.assertEquals(components.getStreet(), "Scarle Rd");
//        Assert.assertEquals(components.getBuildingNumber(), "107");
    }

    private Place createPlace(String formattedAddress) {
        Place res = new Place();

        res.setFormattedAddress(formattedAddress);

        return res;
    }
}