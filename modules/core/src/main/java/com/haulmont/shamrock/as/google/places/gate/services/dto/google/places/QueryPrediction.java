/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google.places;

public class QueryPrediction {
    private FormattableText text;
    private StructuredFormat structuredFormat;

    public QueryPrediction() {
    }

    public QueryPrediction(FormattableText text, StructuredFormat structuredFormat) {
        this.text = text;
        this.structuredFormat = structuredFormat;
    }

    public FormattableText getText() {
        return text;
    }

    public void setText(FormattableText text) {
        this.text = text;
    }

    public StructuredFormat getStructuredFormat() {
        return structuredFormat;
    }

    public void setStructuredFormat(StructuredFormat structuredFormat) {
        this.structuredFormat = structuredFormat;
    }
}