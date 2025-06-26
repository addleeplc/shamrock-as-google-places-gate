/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google.places;

public class FormattableText {
    private String text;
    private StringRange[] matches;

    public FormattableText() {
    }

    public FormattableText(String text, StringRange[] matches) {
        this.text = text;
        this.matches = matches;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public StringRange[] getMatches() {
        return matches;
    }

    public void setMatches(StringRange[] matches) {
        this.matches = matches;
    }
}