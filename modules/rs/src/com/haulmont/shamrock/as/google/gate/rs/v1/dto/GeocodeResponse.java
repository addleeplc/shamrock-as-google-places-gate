/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.rs.v1.dto;

import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.monaco.response.Response;
import com.haulmont.shamrock.address.Address;

/**
 * Created by Nikita Bozhko on 07.01.17.
 * Project Shamrock
 */
public class GeocodeResponse extends Response {
    private Address address;

    public GeocodeResponse(Integer code, String message, Address address) {
        super(code, message);
        this.address = address;
    }

    public GeocodeResponse(ErrorCode code, Address address) {
        super(code.getCode(), code.getMessage());
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }
}
