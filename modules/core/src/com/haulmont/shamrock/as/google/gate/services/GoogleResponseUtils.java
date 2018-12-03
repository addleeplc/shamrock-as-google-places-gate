/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.services;

import com.haulmont.monaco.ServiceException;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.shamrock.as.google.gate.services.dto.google.ResponseStatus;

import java.util.concurrent.Callable;

public class GoogleResponseUtils {
    public static <T> T checkResponse(ResponseStatus status, Callable<T> f) {
        if (status == ResponseStatus.UNKNOWN_ERROR || status == ResponseStatus.UNKNOWN) {
            String message = String.format("Google API responded with non-OK status (status: %s)", status);
            throw new ServiceException(ErrorCode.FAILED_DEPENDENCY, message);
        } else if (status == ResponseStatus.INVALID_REQUEST) {
            String message = String.format("Google API responded with invalid request (status: %s)", status);
            throw new ServiceException(ErrorCode.FAILED_DEPENDENCY, message);
        } else if (status == ResponseStatus.REQUEST_DENIED) {
            String message = String.format("Google API responded with request denied (status: %s)", status);
            throw new ServiceException(ErrorCode.FAILED_DEPENDENCY, message);
        } else if (status == ResponseStatus.OVER_QUERY_LIMIT) {
            String message = String.format("Google API responded with over query limit (status: %s)", status);
            throw new ServiceException(ErrorCode.FAILED_DEPENDENCY, message);
        } else if (status == ResponseStatus.ZERO_RESULTS) {
            return null;
        } else {
            try {
                return f.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
