/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.rs.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.monaco.response.Response;
import com.haulmont.shamrock.as.dto.Address;

import java.util.List;

/**
 * Created by Nikita Bozhko on 01.01.17.
 * Project Shamrock
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponse extends Response {
    @JsonProperty("addresses")
    private List<Address> addresses;

    public SearchResponse(Integer code, String message, List<Address> addresses) {
        super(code, message);
        this.addresses = addresses;
    }

    public SearchResponse(ErrorCode code, List<Address> addresses) {
        super(code.getCode(), code.getMessage());
        this.addresses = addresses;
    }

    public List<Address> getAddresses() {
        return addresses;
    }
}
