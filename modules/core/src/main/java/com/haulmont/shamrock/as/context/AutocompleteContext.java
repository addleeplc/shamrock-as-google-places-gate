/*
 * Copyright 2008 - 2020 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haulmont.shamrock.as.contexts.SearchContext;
import com.haulmont.shamrock.as.dto.CircularRegion;
import com.haulmont.shamrock.as.dto.Location;

public class AutocompleteContext extends SearchContext {

    @JsonProperty("searchRegion")
    private CircularRegion searchRegion;

    @JsonProperty("origin")
    private Location origin;

    public CircularRegion getSearchRegion() {
        return searchRegion;
    }

    public void setSearchRegion(CircularRegion searchRegion) {
        this.searchRegion = searchRegion;
    }

    public Location getOrigin() {
        return origin;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }
}
