/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.utils;

import com.haulmont.shamrock.as.contexts.SearchContext;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.dto.AddressComponents;
import com.haulmont.shamrock.as.dto.AddressData;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class GoogleAddressSearchUtils {
    public static final Pattern GB_POSTCODE_PATTERN = Pattern.compile(
            "^[A-Z]{1,2}[0-9]{1}([A-Z]|[0-9])?\\s{1}[0-9]{1}[A-Z]{2}$",
            Pattern.CASE_INSENSITIVE
    );

    private static final List<String> JUNK_WORDS = Collections.synchronizedList(Arrays.asList(
            "null"
    ));

    public static List<Address> filter(List<Address> addresses) {
        List<Address> res = new ArrayList<>();

        for (Address address : addresses) {
            if (address != null) {
                AddressData data = address.getAddressData();
                if (data != null) {
                    AddressComponents components = data.getAddressComponents();
                    if (components != null) {
                        if (StringUtils.isNotBlank(components.getAddress()) && !containsJunkWords(address)) {
                            String country = components.getCountry();
                            if ("GB".equals(country)) {
                                String postcode = components.getPostcode();
                                if (StringUtils.isNotBlank(postcode) && GB_POSTCODE_PATTERN.matcher(postcode).find()) {
                                    res.add(address);
                                }
                            } else {
                                res.add(address);
                            }
                        }
                    }
                }
            }
        }

        return res;
    }

    private static boolean containsJunkWords(Address a) {
        String s = a.getAddressData().getAddressComponents().getAddress();

        for (String word : JUNK_WORDS) {
            if (StringUtils.containsIgnoreCase(s, word))
                return true;
        }

        return false;
    }

    public static SearchContext clone(SearchContext context) {
        SearchContext clone = new SearchContext();
        clone.setSearchString(context.getSearchString());
        clone.setCountry(context.getCountry());
        clone.setMaxResults(context.getMaxResults());
        clone.setCity(context.getCity());
        clone.setStartIndex(context.getStartIndex());
        clone.setFlatten(context.isFlatten());
        clone.setPreferredCity(context.getPreferredCity());
        clone.setProviders(context.getProviders());
        clone.setSearchBusinessNames(context.isSearchBusinessNames());
        clone.setSearchFlats(context.isSearchFlats());

        return clone;
    }

    private GoogleAddressSearchUtils() {}
}
