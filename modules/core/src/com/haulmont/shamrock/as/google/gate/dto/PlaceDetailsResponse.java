/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.dto;

import java.util.List;

public class PlaceDetailsResponse {
    private String status;
    private PlaceDetailsResult result;
    private List html_attributions;

    public PlaceDetailsResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PlaceDetailsResult getResult() {
        return result;
    }

    public void setResult(PlaceDetailsResult result) {
        this.result = result;
    }

    public List getHtml_attributions() {
        return html_attributions;
    }

    public void setHtml_attributions(List html_attributions) {
        this.html_attributions = html_attributions;
    }

}

