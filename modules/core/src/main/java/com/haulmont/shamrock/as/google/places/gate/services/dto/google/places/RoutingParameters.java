/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services.dto.google.places;

public class RoutingParameters {
    private LatLng origin;
    private TravelMode travelMode;
    private RouteModifiers routeModifiers;
    private RoutingPreference routingPreference;

    public LatLng getOrigin() {
        return origin;
    }

    public void setOrigin(LatLng origin) {
        this.origin = origin;
    }

    public TravelMode getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(TravelMode travelMode) {
        this.travelMode = travelMode;
    }

    public RouteModifiers getRouteModifiers() {
        return routeModifiers;
    }

    public void setRouteModifiers(RouteModifiers routeModifiers) {
        this.routeModifiers = routeModifiers;
    }

    public RoutingPreference getRoutingPreference() {
        return routingPreference;
    }

    public void setRoutingPreference(RoutingPreference routingPreference) {
        this.routingPreference = routingPreference;
    }
}