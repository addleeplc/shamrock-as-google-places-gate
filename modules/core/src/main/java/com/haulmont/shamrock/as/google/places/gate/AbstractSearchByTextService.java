/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate;

import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.dto.AddressComponents;
import com.haulmont.shamrock.as.dto.AddressData;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Inject;

import java.util.Optional;

public class AbstractSearchByTextService {

    @Inject
    protected ServiceConfiguration configuration;

    //

    protected static boolean isParsed(Address address) {
        if (address == null) return false;

        AddressData data = address.getAddressData();
        if (data == null) return false;

        AddressComponents components = data.getAddressComponents();
        if (components == null) return false;

        return StringUtils.isNotBlank(components.getCountry());
    }

    protected boolean isFilterAirports() {
        return Optional.ofNullable(configuration.getFilterAirports()).orElse(Boolean.TRUE);
    }

    protected boolean isRefineNonParsed() {
        return Optional.ofNullable(configuration.getRefineNonParsed()).orElse(Boolean.FALSE);
    }

    protected boolean isFilterNonParsed() {
        return Optional.ofNullable(configuration.geFilterNonParsed()).orElse(Boolean.TRUE);
    }

    protected boolean isEnableParsing() {
        return BooleanUtils.toBooleanDefaultIfNull(configuration.getEnableParsing(), Boolean.TRUE);
    }
}
