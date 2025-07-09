/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.rs.v1.dto;

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

    public SearchResponse(List<Address> addresses) {
        super(ErrorCode.OK);
        this.addresses = addresses;
    }

    public List<Address> getAddresses() {
        return addresses;
    }
}
