/*
 * Copyright 2008 - 2019 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haulmont.monaco.jackson.ObjectMapperContainer;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.dto.AddressData;
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
        PlaceDetails place;
        Address res;
        AddressData data;

        //

        place = load("/place_details_ChIJR7TH7nh0dkgR-AjeE2z9ylk.json");
        res = convert(place);
        data = res.getAddressData();

        Assert.assertEquals(data.getAddressComponents().getPostcode(), "TW13 4RL");
        Assert.assertEquals(data.getAddressComponents().getCity(), "Ashford");

        //

        place = load("/place_details_ChIJ49kVQ3d0dkgR8tx5cAC2L0Y.json");
        res = convert(place);
        data = res.getAddressData();

        Assert.assertEquals(data.getFormattedAddress(), "343 Feltham Hill Road, Ashford, TW15 1LP");
        Assert.assertEquals(data.getAddressComponents().getAddress(), "343 Feltham Hill Road");

        Assert.assertEquals(data.getAddressComponents().getBuildingNumber(), "343");
        Assert.assertEquals(data.getAddressComponents().getStreet(), "Feltham Hill Road");
        Assert.assertEquals(data.getAddressComponents().getPostcode(), "TW15 1LP");
        Assert.assertEquals(data.getAddressComponents().getCity(), "Ashford");
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