/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.utils;

import com.haulmont.shamrock.address.Address;
import com.haulmont.shamrock.address.context.SearchContext;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class GoogleAddressSearchUtils {
    public static final Pattern GB_POSTCODE_PATTERN = Pattern.compile(
            "^[A-Z]{1,2}[0-9]{1}([A-Z]|[0-9])?\\s{1}[0-9]{1}[A-Z]{2}$",
            Pattern.CASE_INSENSITIVE
    );

    private static final List<String> JUNK_WORDS = Collections.synchronizedList(Arrays.asList(
            "null"
    ));

    public static List<Address> filter(List<Address> addresses) {
        return addresses.parallelStream()
                .filter(Objects::nonNull)
                .filter(address -> address.getAddressData() != null)
                .filter(address -> address.getAddressData().getAddressComponents() != null)
                .filter(address -> StringUtils.isNotBlank(address.getAddressData().getAddressComponents().getAddress()))
                .filter(address -> !containsJunkWords(address))
                .filter(address -> !"GB".equals(address.getAddressData().getAddressComponents().getCountry()) || GB_POSTCODE_PATTERN.matcher(address.getAddressData().getAddressComponents().getPostcode()).find())
                .collect(Collectors.toList());
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
