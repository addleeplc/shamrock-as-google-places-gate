/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.parsers;

import com.haulmont.shamrock.as.dto.AddressComponents;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PlaceParser_UKTest extends AbstractPlaceParserTest {

    @Test
    public void test() {
        PlaceParser parser = getParser();

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

        components = parser.parse(createPlace("Guinness Road Trading Estate, Guinness Rd, Stretford, Manchester M17 1SB, UK"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getPostcode(), "M17 1SB");
        Assert.assertEquals(components.getCity(), "Manchester");
        Assert.assertEquals(components.getAddress(), "Guinness Road Trading Estate, Guinness Rd");
        Assert.assertEquals(components.getStreet(), "Guinness Rd");

        //

        components = parser.parse(createPlace("107 Scarle Rd, Wembley HA0 4SS, UK"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getPostcode(), "HA0 4SS");
        Assert.assertEquals(components.getCity(), "Wembley");
        Assert.assertEquals(components.getAddress(), "107 Scarle Rd");
        Assert.assertEquals(components.getStreet(), "Scarle Rd");
//        Assert.assertEquals(components.getBuildingNumber(), "107");
    }

    @Test
    public void testAutocomplete() {
        PlaceParser parser = getParser();
        AddressComponents components;

        //

        components = parser.parse(createPlace("Euston Square Hotel, North Gower Street, London, UK"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "GB");
        Assert.assertNull(components.getPostcode());
        Assert.assertEquals(components.getCity(), "London");
        Assert.assertEquals(components.getAddress(), "Euston Square Hotel, North Gower Street");
        Assert.assertEquals(components.getStreet(), "North Gower Street");

        //

        components = parser.parse(createPlace("Euston Square Station, London, UK"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "GB");
        Assert.assertNull(components.getPostcode());
        Assert.assertEquals(components.getCity(), "London");
//        Assert.assertEquals(components.getAddress(), "Euston Square Station");
        Assert.assertNull(components.getStreet());


        //

        components = parser.parse(createPlace("AA Admirals Club Lounge Heathrow Terminal 3, Longford, Hounslow, UK"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "GB");

        //

        components = parser.parse(createPlace("Novotel London Heathrow Airport T1 T2 and T3, Bath Road, Heathrow, Hayes, UK"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "GB");

        //

        components = parser.parse(createPlace("Marks & Spencer HEATHROW AIRPORT T3 SIMPLY FOOD, Arrivals Lounge, Nelson Road, Hounslow, UK"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "GB");

        //

        components = parser.parse(createPlace("Heathrow Terminal 3, Hounslow, UK"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "GB");

        //

        components = parser.parse(createPlace("Heathrow Short Stay Parking Terminal 3, Heathrow Airport (LHR), Camberley Road, Hounslow, UK"));

        Assert.assertNotNull(components);
        Assert.assertEquals(components.getCountry(), "GB");

        //

        components = parser.parse(createPlace("TailWings Pet Travel, Heathrow Airport, Hounslow, UK"));

        Assert.assertNotNull(components);
        Assert.assertNull(components.getStreet());
        Assert.assertEquals(components.getCity(), "Hounslow");
        Assert.assertEquals(components.getCountry(), "GB");

        //

        components = parser.parse(createPlace("Marks & Spencer SUNBURY CROSS SIMPLY FOOD, Staines Road West, Sunbury-on-Thames, UK"));

        Assert.assertNotNull(components);
        Assert.assertNull(components.getStreet());
        Assert.assertEquals(components.getCity(), "Sunbury-on-Thames");
        Assert.assertEquals(components.getCountry(), "GB");

        //

        components = parser.parse(createPlace("M&S Simply Food, Hounslow, UK"));

        Assert.assertNotNull(components);
        Assert.assertNull(components.getStreet());
        Assert.assertEquals(components.getCity(), "Hounslow");
        Assert.assertEquals(components.getCountry(), "GB");

    }

    protected PlaceParser getParser() {
        return new PlaceParser_UK();
    }

}