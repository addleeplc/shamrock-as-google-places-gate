/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser.ru;

import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parser.DefaultGoogleAddressParser;
import com.haulmont.shamrock.as.google.gate.parser.Parser;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Parser("RU")
public class GoogleAddressParser extends DefaultGoogleAddressParser {
    private static final Map<String, String> transliterationTable = new HashMap<>();
    static {
        transliterationTable.put("А", "A");
        transliterationTable.put("а", "a");

        transliterationTable.put("Б", "B");
        transliterationTable.put("б", "b");

        transliterationTable.put("В", "V");
        transliterationTable.put("в", "v");

        transliterationTable.put("Г", "G");
        transliterationTable.put("г", "g");

        transliterationTable.put("Д", "D");
        transliterationTable.put("д", "d");

        transliterationTable.put("Е", "E");
        transliterationTable.put("е", "e");

        transliterationTable.put("Ё", "Yo");
        transliterationTable.put("ё", "yo");

        transliterationTable.put("Ж", "Zh");
        transliterationTable.put("ж", "zh");

        transliterationTable.put("З", "Z");
        transliterationTable.put("з", "z");

        transliterationTable.put("И", "I");
        transliterationTable.put("и", "i");

        transliterationTable.put("Й", "J");
        transliterationTable.put("й", "j");

        transliterationTable.put("К", "K");
        transliterationTable.put("к", "k");

        transliterationTable.put("Л", "L");
        transliterationTable.put("л", "l");

        transliterationTable.put("М", "M");
        transliterationTable.put("м", "m");

        transliterationTable.put("Н", "N");
        transliterationTable.put("н", "n");

        transliterationTable.put("О", "O");
        transliterationTable.put("о", "o");

        transliterationTable.put("П", "P");
        transliterationTable.put("п", "p");

        transliterationTable.put("Р", "R");
        transliterationTable.put("р", "r");

        transliterationTable.put("С", "S");
        transliterationTable.put("с", "s");

        transliterationTable.put("Т", "T");
        transliterationTable.put("т", "t");

        transliterationTable.put("У", "U");
        transliterationTable.put("у", "u");

        transliterationTable.put("Ф", "F");
        transliterationTable.put("ф", "f");

        transliterationTable.put("Х", "Kh");
        transliterationTable.put("х", "kh");

        transliterationTable.put("Ц", "Ts");
        transliterationTable.put("ц", "ts");

        transliterationTable.put("Ч", "Ch");
        transliterationTable.put("ч", "ch");

        transliterationTable.put("Ш", "Sh");
        transliterationTable.put("ш", "sh");

        transliterationTable.put("Щ", "Shch");
        transliterationTable.put("щ", "shch");

        transliterationTable.put("Ъ", "ʺ");
        transliterationTable.put("ъ", "ʺ");

        transliterationTable.put("Ы", "Y");
        transliterationTable.put("ы", "y");

        transliterationTable.put("Ь", "ʹ");
        transliterationTable.put("ь", "ʹ");

        transliterationTable.put("Э", "Eh");
        transliterationTable.put("э", "eh");

        transliterationTable.put("Ю", "Yu");
        transliterationTable.put("ю", "yu");

        transliterationTable.put("Я", "Ya");
        transliterationTable.put("я", "ya");
    }

    @Override
    protected void prepareComponents(Map<String, AddressComponent> components) {
        super.prepareComponents(components);
        transliterateComponents(components, transliterationTable);
    }

    @Override
    protected String parseStreet(String formattedAddress, Map<String, AddressComponent> components) {
        String street = super.parseStreet(formattedAddress, components);
        if (StringUtils.isNotBlank(street)) {
            street = street.replace("ulitsa", "")
                    .replace("ul.", "")
                    .trim();
        }

        return street;
    }

    @Override
    protected String parseCity(Map<String, AddressComponent> components) {
        String city = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
        if (StringUtils.isBlank(city))
            city = getFirstLong(components, GElement.locality, GElement.political);

        return city;
    }
}
