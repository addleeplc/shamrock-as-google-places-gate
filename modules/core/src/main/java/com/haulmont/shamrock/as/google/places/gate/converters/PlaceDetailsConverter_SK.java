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

import java.util.Map;

@Component
@PlaceDetailsConverter.Component(country = "SK")
public class PlaceDetailsConverter_SK extends DefaultPlaceDetailsConverter {
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
