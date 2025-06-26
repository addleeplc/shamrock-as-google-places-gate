/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.rs.v1.dto;

import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.monaco.response.Response;
import com.haulmont.shamrock.as.dto.Address;

/*
 * Author: Nikita Bozhko
 * Created: 09/01/2017 11:40
 */
public class RefineResponse extends Response {
    private final Address address;

    public RefineResponse(ErrorCode code, Address address) {
        super(code.getCode(), code.getMessage());
        this.address = address;
    }

    public RefineResponse(Integer code, String message, Address address) {
        super(code, message);
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }
}
