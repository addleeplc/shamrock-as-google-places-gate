/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.converters;

import com.haulmont.shamrock.as.google.places.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.places.gate.dto.enums.GElement;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@PlaceDetailsConverter.Component(country = "AU")
public class PlaceDetailsConverter_AU extends DefaultPlaceDetailsConverter {
    protected static final Set<String> MELBOURNE_SUB_URBANS = new HashSet<>(
            Arrays.asList(
                    "Docklands", 
                    "Docklands 3008", 
                    "Port Phillip", 
                    "Yarra", 
                    "Banyule", 
                    "Darebin", 
                    "Hume", 
                    "Moonee Valley", 
                    "Moreland City", 
                    "Nillumbik", 
                    "Whittlesea", 
                    "Boroondara", 
                    "Knox", 
                    "Manningham", 
                    "Maroondah", 
                    "Whitehorse", 
                    "Yarra Ranges", 
                    "Bayside", 
                    "Cardinia", 
                    "Casey", 
                    "Greater Dandenong", 
                    "Frankston", 
                    "Glen Eira", 
                    "Kingston", 
                    "Monash", 
                    "Mornington Peninsula", 
                    "Stonnington", 
                    "Brimbank", 
                    "Hobsons Bay", 
                    "Maribyrnong", 
                    "Melton", 
                    "Wyndham"
            )
    );
    
    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String city = getFirstLong(components, GElement.locality, GElement.political);
        if (StringUtils.equalsIgnoreCase(city, "Melbourne"))
            return city;

        city = getFirstShort(components, GElement.administrative_area_level_2, GElement.political);

        return isMelbourne(city) ? "Melbourne" : getFirstLong(components, GElement.locality, GElement.political);
    }

    private boolean isMelbourne(String suburb) {
        if (StringUtils.containsIgnoreCase(suburb, "melbourne"))
            return true;

        for (String subUrban : MELBOURNE_SUB_URBANS) {
            if (StringUtils.equalsIgnoreCase(suburb, subUrban) || StringUtils.containsIgnoreCase(suburb, subUrban))
                return true;
        }

        return false;
    }
}
