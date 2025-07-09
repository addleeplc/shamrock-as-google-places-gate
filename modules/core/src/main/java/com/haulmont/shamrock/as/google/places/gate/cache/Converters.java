/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.cache;

import com.haulmont.shamrock.as.context.AutocompleteContext;
import com.haulmont.shamrock.as.contexts.GeoRegion;
import com.haulmont.shamrock.as.contexts.SearchContext;
import com.haulmont.shamrock.as.google.places.gate.cache.dto.GeneralSearchContext;
import com.haulmont.shamrock.as.google.places.gate.dto.RefineContext;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.Circle;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.Geometry;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.LatLng;

public class Converters {
    public static GeneralSearchContext forSearch(SearchContext source) {
        if (source == null) {
            return null;
        }
        GeneralSearchContext res = new GeneralSearchContext("search");
        res.setSearchString(source.getSearchString());
        res.setCity(source.getCity());
        res.setCountry(source.getCountry());
        res.setPostcode(source.getPostcode());
        res.setPreferredCity(source.getPreferredCity());
        res.setPreferredCountry(source.getPreferredCountry());

        return res;
    }

    public static GeneralSearchContext forAutocomplete(AutocompleteContext source) {
        if (source == null) {
            return null;
        }
        GeneralSearchContext res = new GeneralSearchContext("autocomplete");
        res.setSearchString(source.getSearchString());
        if(source.getSearchRegion()!=null) {
            LatLng latLng = new LatLng(source.getSearchRegion().getLat(), source.getSearchRegion().getLon());
            res.setLocationBias(new Geometry(new Circle(latLng, source.getSearchRegion().getRadius())));
        }
        if(source.getOrigin()!=null)
            res.setLocation(new LatLng(source.getOrigin().getLat(), source.getOrigin().getLon()));
        res.setCountry(source.getCountry());
        res.setAddress(source.getSearchString());
        return res;
    }

    public static GeneralSearchContext forReverseGeocode(GeoRegion source) {
        if (source == null) {
            return null;
        }

        GeneralSearchContext res = new GeneralSearchContext("r-geo");
        res.setLocationBias(new Geometry(new Circle(new LatLng(source.getLatitude(), source.getLongitude()), source.getRadius())));
        return res;
    }

    public static GeneralSearchContext forRefine(RefineContext context) {
        GeneralSearchContext res = new GeneralSearchContext("refine");
        res.setPreferGeocoding(context.isPreferGeocoding());
        res.setPlaceId(context.getPlaceId());
        res.setAddress(context.getFormattedAddress());
        return res;
    }

    public static String asString(Double value) {
        return value != null ? String.format("%.4f", value) : null;
    }
}