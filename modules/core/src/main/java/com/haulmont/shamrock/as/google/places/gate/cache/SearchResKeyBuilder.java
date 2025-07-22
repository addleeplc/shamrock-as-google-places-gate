/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.cache;

import com.haulmont.shamrock.as.context.AutocompleteContext;
import com.haulmont.shamrock.as.contexts.GeoRegion;
import com.haulmont.shamrock.as.contexts.SearchContext;
import com.haulmont.shamrock.as.google.places.gate.dto.RefineContext;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.Circle;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.Geometry;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.LatLng;

public class SearchResKeyBuilder {

    private static final int COORDINATES_PRECISION = 5;
    private static final String COORDINATES_FORMAT = "%." + COORDINATES_PRECISION + "f";

    //

    public static SearchResKey buildFrom(SearchContext source) {
        if (source == null) return null;

        SearchResKey res = new SearchResKey("search");

        res.setSearchString(source.getSearchString());
        res.setCity(source.getCity());
        res.setCountry(source.getCountry());
        res.setPostcode(source.getPostcode());
        res.setPreferredCity(source.getPreferredCity());
        res.setPreferredCountry(source.getPreferredCountry());

        return res;
    }

    public static SearchResKey buildFrom(AutocompleteContext source) {
        if (source == null) return null;

        SearchResKey res = new SearchResKey("autocomplete");

        res.setSearchString(source.getSearchString());
        if (source.getSearchRegion() != null) {
            LatLng latLng = new LatLng(source.getSearchRegion().getLat(), source.getSearchRegion().getLon());
            res.setLocationBias(new Geometry(new Circle(latLng, source.getSearchRegion().getRadius())));
        }
        if (source.getOrigin() != null) {
            res.setLocation(new LatLng(source.getOrigin().getLat(), source.getOrigin().getLon()));
        }
        res.setCountry(source.getCountry());
        res.setAddress(source.getSearchString());

        return res;
    }

    public static SearchResKey buildFrom(GeoRegion source) {
        if (source == null) return null;

        SearchResKey res = new SearchResKey("nearby");
        res.setLocationBias(new Geometry(new Circle(new LatLng(source.getLatitude(), source.getLongitude()), source.getRadius())));
        return res;
    }

    public static SearchResKey buildFrom(RefineContext context) {
        SearchResKey res = new SearchResKey("refine");

        res.setPlaceId(context.getPlaceId());
        res.setAddress(context.getFormattedAddress());

        return res;
    }

    public static String toString(Double value) {
        return value != null ? String.format(COORDINATES_FORMAT, value) : null;
    }
}