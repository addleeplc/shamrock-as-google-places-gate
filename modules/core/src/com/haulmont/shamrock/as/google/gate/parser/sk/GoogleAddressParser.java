/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.sk;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Parser("SK")
public class GoogleAddressParser extends DefaultGoogleAddressParser {
    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String cityValue = getFirstLong(components, GElement.sublocality_level_1, GElement.sublocality, GElement.political);
        if (StringUtils.isNotBlank(cityValue) && StringUtils.equalsAnyIgnoreCase(
                cityValue,
                "Ružinov", "Nové Mesto", "Devínska Nová Ves", "Staré Mesto", "Podunajské Biskupice",
                "Vrakuňa", "Rača", "Vajnory", "Devín", "Dúbravka", "Karlova Ves", "Lamač", "Záhorská Bystrica",
                "Čunovo", "Jarovce", "Petržalka", "Rusovce")
                ) {
            cityValue = "Bratislava";
        }

        return cityValue;
    }
}
