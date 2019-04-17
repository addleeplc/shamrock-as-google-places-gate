/*
 * Copyright 2008 - 2019 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haulmont.monaco.jackson.ObjectMapperContainer;
import com.haulmont.shamrock.address.Address;
import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.services.dto.google.places.PlaceDetailsResponse;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class PlaceDetailsConverter_GBTest {
    @Test
    public void test() throws IOException {
        PlaceDetails place = load("/place_details_ChIJR7TH7nh0dkgR-AjeE2z9ylk.json");

        Address res = convert(place);

        Assert.assertEquals(res.getAddressData().getAddressComponents().getPostcode(), "TW13 4RL");
        Assert.assertEquals(res.getAddressData().getAddressComponents().getCity(), "Ashford");

//        System.out.println("res = " + res);
    }

    private PlaceDetails load(String resource) throws IOException {
        InputStream is = getClass().getResourceAsStream(resource);

        ObjectMapper mapper = new ObjectMapperContainer().mapper();

        PlaceDetailsResponse placeDetails = mapper.readerFor(PlaceDetailsResponse.class).readValue(is);
        return placeDetails.getResult();
    }

    private Address convert(PlaceDetails place) {
        PlaceDetailsConverter_GB converter = new PlaceDetailsConverter_GB();

        Map<String, AddressComponent> components = GoogleAddressUtils.convert(place.getAddressComponents());

        String country = GoogleAddressUtils.getFirstShort(components, GElement.country, GElement.political);
        if (StringUtils.isBlank(country)) {
            throw new RuntimeException("Country is null");
        }

        return converter.convert(place, components);
    }
}