/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.resources.v1.dto;

import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.monaco.response.Response;
import com.haulmont.shamrock.as.dto.Address;

import java.util.List;

/**
 * Created by Nikita Bozhko on 07.01.17.
 * Project Shamrock
 */
public class ReverseGeocodingResponse extends Response {
    private final List<Address> addresses;

    public ReverseGeocodingResponse(Integer code, String message, List<Address> addresses) {
        super(code, message);
        this.addresses = addresses;
    }

    public ReverseGeocodingResponse(ErrorCode code, List<Address> addresses) {
        super(code.getCode(), code.getMessage());
        this.addresses = addresses;
    }

    public List<Address> getAddresses() {
        return addresses;
    }
}
