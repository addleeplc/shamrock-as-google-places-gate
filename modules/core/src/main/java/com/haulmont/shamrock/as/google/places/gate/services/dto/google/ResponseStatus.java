/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google;

public enum ResponseStatus {
    OK,
    ZERO_RESULTS,
    OVER_QUERY_LIMIT,
    REQUEST_DENIED,
    INVALID_REQUEST,
    UNKNOWN_ERROR,
    UNKNOWN;

    public static ResponseStatus fromString(ResponseStatus res) {
        for (ResponseStatus status : values()) {
            if (status.equals(res)) {
                return status;
            }
        }

        return UNKNOWN;
    }
}
