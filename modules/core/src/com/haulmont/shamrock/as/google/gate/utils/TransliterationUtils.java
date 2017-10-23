/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.utils;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public final class TransliterationUtils {

    //Russian

    private static final Map<String, String> ru_transliterationTable = new HashMap<>();
    static {
        ru_transliterationTable.put("А", "A");
        ru_transliterationTable.put("а", "a");

        ru_transliterationTable.put("Б", "B");
        ru_transliterationTable.put("б", "b");

        ru_transliterationTable.put("В", "V");
        ru_transliterationTable.put("в", "v");

        ru_transliterationTable.put("Г", "G");
        ru_transliterationTable.put("г", "g");

        ru_transliterationTable.put("Д", "D");
        ru_transliterationTable.put("д", "d");

        ru_transliterationTable.put("Е", "E");
        ru_transliterationTable.put("е", "e");

        ru_transliterationTable.put("Ё", "Yo");
        ru_transliterationTable.put("ё", "yo");

        ru_transliterationTable.put("Ж", "Zh");
        ru_transliterationTable.put("ж", "zh");

        ru_transliterationTable.put("З", "Z");
        ru_transliterationTable.put("з", "z");

        ru_transliterationTable.put("И", "I");
        ru_transliterationTable.put("и", "i");

        ru_transliterationTable.put("Й", "J");
        ru_transliterationTable.put("й", "j");

        ru_transliterationTable.put("К", "K");
        ru_transliterationTable.put("к", "k");

        ru_transliterationTable.put("Л", "L");
        ru_transliterationTable.put("л", "l");

        ru_transliterationTable.put("М", "M");
        ru_transliterationTable.put("м", "m");

        ru_transliterationTable.put("Н", "N");
        ru_transliterationTable.put("н", "n");

        ru_transliterationTable.put("О", "O");
        ru_transliterationTable.put("о", "o");

        ru_transliterationTable.put("П", "P");
        ru_transliterationTable.put("п", "p");

        ru_transliterationTable.put("Р", "R");
        ru_transliterationTable.put("р", "r");

        ru_transliterationTable.put("С", "S");
        ru_transliterationTable.put("с", "s");

        ru_transliterationTable.put("Т", "T");
        ru_transliterationTable.put("т", "t");

        ru_transliterationTable.put("У", "U");
        ru_transliterationTable.put("у", "u");

        ru_transliterationTable.put("Ф", "F");
        ru_transliterationTable.put("ф", "f");

        ru_transliterationTable.put("Х", "Kh");
        ru_transliterationTable.put("х", "kh");

        ru_transliterationTable.put("Ц", "Ts");
        ru_transliterationTable.put("ц", "ts");

        ru_transliterationTable.put("Ч", "Ch");
        ru_transliterationTable.put("ч", "ch");

        ru_transliterationTable.put("Ш", "Sh");
        ru_transliterationTable.put("ш", "sh");

        ru_transliterationTable.put("Щ", "Shch");
        ru_transliterationTable.put("щ", "shch");

        ru_transliterationTable.put("Ъ", "ʺ");
        ru_transliterationTable.put("ъ", "ʺ");

        ru_transliterationTable.put("Ы", "Y");
        ru_transliterationTable.put("ы", "y");

        ru_transliterationTable.put("Ь", "ʹ");
        ru_transliterationTable.put("ь", "ʹ");

        ru_transliterationTable.put("Э", "Eh");
        ru_transliterationTable.put("э", "eh");

        ru_transliterationTable.put("Ю", "Yu");
        ru_transliterationTable.put("ю", "yu");

        ru_transliterationTable.put("Я", "Ya");
        ru_transliterationTable.put("я", "ya");
    }

    public static String ru_transliterate(String origin) {
        return transliterate(origin, ru_transliterationTable);
    }

    //Bulgarian

    private static final Map<String, String> bg_transliterationTable = new HashMap<>();
    static {
        bg_transliterationTable.put("А", "A");
        bg_transliterationTable.put("а", "a");

        bg_transliterationTable.put("Б", "B");
        bg_transliterationTable.put("б", "b");

        bg_transliterationTable.put("В", "V");
        bg_transliterationTable.put("в", "v");

        bg_transliterationTable.put("Г", "G");
        bg_transliterationTable.put("г", "g");

        bg_transliterationTable.put("Д", "D");
        bg_transliterationTable.put("д", "d");

        bg_transliterationTable.put("Е", "E");
        bg_transliterationTable.put("е", "e");

        bg_transliterationTable.put("Ж", "Zh");
        bg_transliterationTable.put("ж", "zh");

        bg_transliterationTable.put("З", "Z");
        bg_transliterationTable.put("з", "z");

        bg_transliterationTable.put("И", "I");
        bg_transliterationTable.put("и", "i");

        bg_transliterationTable.put("К", "K");
        bg_transliterationTable.put("к", "k");

        bg_transliterationTable.put("Л", "L");
        bg_transliterationTable.put("л", "l");

        bg_transliterationTable.put("М", "M");
        bg_transliterationTable.put("м", "m");

        bg_transliterationTable.put("Н", "N");
        bg_transliterationTable.put("н", "n");

        bg_transliterationTable.put("О", "O");
        bg_transliterationTable.put("о", "o");

        bg_transliterationTable.put("П", "P");
        bg_transliterationTable.put("п", "p");

        bg_transliterationTable.put("Р", "R");
        bg_transliterationTable.put("р", "r");

        bg_transliterationTable.put("С", "S");
        bg_transliterationTable.put("с", "s");

        bg_transliterationTable.put("Т", "T");
        bg_transliterationTable.put("т", "t");

        bg_transliterationTable.put("Тс", "T-s");
        bg_transliterationTable.put("тс", "t-s");

        bg_transliterationTable.put("У", "U");
        bg_transliterationTable.put("у", "u");

        bg_transliterationTable.put("Ф", "F");
        bg_transliterationTable.put("ф", "f");

        bg_transliterationTable.put("Х", "Kh");
        bg_transliterationTable.put("х", "kh");

        bg_transliterationTable.put("Ц", "Ts");
        bg_transliterationTable.put("ц", "ts");

        bg_transliterationTable.put("Ч", "Ch");
        bg_transliterationTable.put("ч", "ch");

        bg_transliterationTable.put("Ш", "Sh");
        bg_transliterationTable.put("ш", "sh");

        bg_transliterationTable.put("Щ", "Sht");
        bg_transliterationTable.put("щ", "sht");

        bg_transliterationTable.put("Ъ", "Ŭ");
        bg_transliterationTable.put("ъ", "ŭ");

        bg_transliterationTable.put("Ы", "Y");
        bg_transliterationTable.put("ы", "y");

        bg_transliterationTable.put("Ь", "’");
        bg_transliterationTable.put("ь", "’");

        bg_transliterationTable.put("Ю", "Yu");
        bg_transliterationTable.put("ю", "yu");

        bg_transliterationTable.put("Я", "Ya");
        bg_transliterationTable.put("я", "ya");

        bg_transliterationTable.put("Ѣ", "Ê");
        bg_transliterationTable.put("ѣ", "ê");

        bg_transliterationTable.put("Ѫ", "Ū");
        bg_transliterationTable.put("ѫ", "ū");
    }
    
    public static String bg_transliterate(String origin) {
        return transliterate(origin, bg_transliterationTable);
    }

    private static String transliterate(String origin, Map<String, String> transliterationTable) {
        if (StringUtils.isBlank(origin))
            return origin;

        boolean b = false;
        for (String s : transliterationTable.keySet()) {
            if (origin.contains(s)) {
                b = true;
                break;
            }
        }

        if (b) {
            char[] chars = origin.toCharArray();
            StringBuilder sb = new StringBuilder();
            for (char ch : chars) {
                if (transliterationTable.containsKey(String.valueOf(ch)))
                    sb.append(transliterationTable.get(String.valueOf(ch)));
                else
                    sb.append(ch);
            }

            return sb.toString();
        } else {
            return origin;
        }
    }

    private TransliterationUtils() {}
}
