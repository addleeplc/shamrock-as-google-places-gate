/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google.places;

public class Suggestion {
    private PlacePrediction placePrediction;
    private QueryPrediction queryPrediction;

    public PlacePrediction getPlacePrediction() {
        return placePrediction;
    }

    public void setPlacePrediction(PlacePrediction placePrediction) {
        this.placePrediction = placePrediction;
    }

    public QueryPrediction getQueryPrediction() {
        return queryPrediction;
    }

    public void setQueryPrediction(QueryPrediction queryPrediction) {
        this.queryPrediction = queryPrediction;
    }
}