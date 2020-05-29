/*
 * Copyright
 */

package com.haulmont.bali.lang;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;

import java.util.HashMap;
import java.util.Map;

public class MapUtils {
    public static BidiMap<String, String> bidiMapOf(String...values) {
        int mod = values.length % 2;
        if (mod != 0) throw new IllegalArgumentException();

        Map<String, String> res = new HashMap<>();

        for (int i = 0; i < (values.length / 2 - 1); i++) {
            res.put(values[i * 2], values[i * 2 + 1]);
        }

        return new TreeBidiMap<>(res);
    }
}
