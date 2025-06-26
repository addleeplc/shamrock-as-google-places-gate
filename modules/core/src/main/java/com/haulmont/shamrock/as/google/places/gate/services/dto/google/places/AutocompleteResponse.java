/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google.places;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.ResponseStatus;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AutocompleteResponse {
    private List<Suggestion> suggestions;
    private ResponseStatus status;

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }
}