/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.utils;

import java.util.HashMap;
import java.util.Map;

public final class Constants {
    public static final class Country {
        public static final Map<String, String> isoToCountry = new HashMap<>();
        static {
            isoToCountry.put("GB", "United Kingdom");
            isoToCountry.put("US", "United States");
            isoToCountry.put("FR", "France");
            isoToCountry.put("ES", "Spain");
            isoToCountry.put("DE", "Germany");
            isoToCountry.put("CH", "Switzerland");
            isoToCountry.put("NL", "Netherlands");
            isoToCountry.put("IT", "Italy");
            isoToCountry.put("IE", "Ireland");
            isoToCountry.put("SE", "Sweden");
            isoToCountry.put("DK", "Denmark");
            isoToCountry.put("BE", "Belgium");
            isoToCountry.put("CA", "Canada");
        }

        private Country() {}
    }

    private Constants() {}
}
