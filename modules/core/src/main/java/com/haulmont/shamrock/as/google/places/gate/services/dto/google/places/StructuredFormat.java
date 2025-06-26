/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google.places;

public class StructuredFormat {
    private FormattableText mainText;
    private FormattableText secondaryText;

    public StructuredFormat() {
    }

    public StructuredFormat(FormattableText mainText, FormattableText secondaryText) {
        this.mainText = mainText;
        this.secondaryText = secondaryText;
    }

    public FormattableText getMainText() {
        return mainText;
    }

    public void setMainText(FormattableText mainText) {
        this.mainText = mainText;
    }

    public FormattableText getSecondaryText() {
        return secondaryText;
    }

    public void setSecondaryText(FormattableText secondaryText) {
        this.secondaryText = secondaryText;
    }
}