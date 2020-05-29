/*
 * Copyright
 */

package com.haulmont.shamrock.geo.utils;

import com.haulmont.shamrock.geo.PostcodeHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostalCodeUtils {
    public static class GB {
        public static String parse(String s) {
            return PostcodeHelper.parsePostcode(s, false);
        }
    }

    public static class US {
        private final static Pattern PATTERN = Pattern.compile("(\\d{5}([\\-]\\d{4})?)");

        public static String parse(String s) {
            Matcher matcher = PATTERN.matcher(s);
            return matcher.find() ? matcher.group() : null;
        }
    }

    public static class DE {
        private final static Pattern PATTERN = Pattern.compile("[0-9]{5}");

        public static String parse(String s) {
            Matcher matcher = PATTERN.matcher(s);
            return matcher.find() ? matcher.group() : null;
        }
    }

    public static class FR {
        private final static Pattern PATTERN = Pattern.compile("[0-9]{5}");

        public static String parse(String s) {
            Matcher matcher = PATTERN.matcher(s);
            return matcher.find() ? matcher.group() : null;
        }
    }

    public static class IT {
        private final static Pattern PATTERN = Pattern.compile("[0-9]{5}");

        public static String parse(String s) {
            Matcher matcher = PATTERN.matcher(s);
            return matcher.find() ? matcher.group() : null;
        }
    }

    public static class IE {
        private final static Pattern PATTERN = Pattern.compile("[A-Z0-9]{3} [A-Z0-9]{4}");

        public static String parse(String s) {
            Matcher matcher = PATTERN.matcher(s);
            return matcher.find() ? matcher.group() : null;
        }
    }

}
