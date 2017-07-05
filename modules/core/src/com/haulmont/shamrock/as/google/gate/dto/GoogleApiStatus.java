/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.dto;

public enum GoogleApiStatus {
    OK,
    ZERO_RESULTS,
    OVER_QUERY_LIMIT,
    REQUEST_DENIED,
    INVALID_REQUEST,
    UNKNOWN_ERROR,
    UNKNOWN;

    public static GoogleApiStatus fromString(String res) {
        for (GoogleApiStatus status : values()) {
            if (status.name().equals(res)) {
                return status;
            }
        }

        return UNKNOWN;
    }
}
