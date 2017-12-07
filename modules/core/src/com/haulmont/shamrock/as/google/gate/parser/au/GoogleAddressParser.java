/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.au;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Parser("AU")
public class GoogleAddressParser extends DefaultGoogleAddressParser {
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
