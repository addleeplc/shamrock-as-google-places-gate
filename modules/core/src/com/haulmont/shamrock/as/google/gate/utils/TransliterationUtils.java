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
        if (StringUtils.isBlank(origin))
            return origin;

        boolean b = false;
        for (String s : ru_transliterationTable.keySet()) {
            if (origin.contains(s)) {
                b = true;
                break;
            }
        }

        if (b) {
            char[] chars = origin.toCharArray();
            StringBuilder sb = new StringBuilder();
            for (char ch : chars) {
                if (ru_transliterationTable.containsKey(String.valueOf(ch)))
                    sb.append(ru_transliterationTable.get(String.valueOf(ch)));
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
