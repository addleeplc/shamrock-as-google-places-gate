/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services;

import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.config.annotations.Config;
import com.haulmont.monaco.config.annotations.Property;
import com.haulmont.monaco.unirest.ServiceCallUtils;
import com.haulmont.monaco.unirest.UnirestCommand;
import com.haulmont.shamrock.as.google.places.gate.config.GoogleConfigurationService;
import com.haulmont.shamrock.as.google.places.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.places.gate.dto.Geometry;
import com.haulmont.shamrock.as.google.places.gate.dto.Place;
import com.haulmont.shamrock.as.google.places.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.geocoding.GeocodePlaceByIdResponse;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.LatLng;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.PlaceDetailsResponse;
import kong.unirest.GetRequest;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class GoogleGeocodingService {
    private static final String SERVICE_NAME = "google-geocode-api";

    @Inject
    private ServiceConfig configuration;

    @Inject
    private GoogleConfigurationService googleConfigurationService;

    //

    public PlaceDetails getPlaceDetails(String id) {
        GeocodePlaceByIdResponse response = new GoogleGeocodePlaceDetailsCommand(id).execute();

        if (GoogleResponseUtils.isResponseOk(response.getStatus())) {
            List<com.haulmont.shamrock.as.google.places.gate.services.dto.google.geocoding.PlaceDetails> list = response.getResults();
            if (list != null && !list.isEmpty())
                return convert(list.get(0));
        }
        return null;
    }


    private PlaceDetails convert(com.haulmont.shamrock.as.google.places.gate.services.dto.google.geocoding.PlaceDetails place) {
        if (place == null || place.getAddressComponents() == null || place.getAddressComponents().isEmpty())
            return null;

        PlaceDetails res = new PlaceDetails();
        res.setAddressComponents(place.getAddressComponents().stream().map(this::convert).collect(Collectors.toList()));
        res.setFormattedAddress(place.getFormattedAddress());
        res.setId(place.getPlaceId());
        res.setTypes(place.getTypes());
        res.setDisplayName(new Place.DisplayName(place.getName()));
        res.setLocation(convert(place.getGeometry()));

        return res;
    }

    private LatLng convert(Geometry geometry) {
        if (geometry == null || geometry.getLocation() == null)
            return null;

        LatLng res = new LatLng();
        res.setLatitude(geometry.getLocation().getLat());
        res.setLongitude(geometry.getLocation().getLng());
        return res;
    }

    private AddressComponent convert(com.haulmont.shamrock.as.google.places.gate.services.dto.google.geocoding.AddressComponent component) {
        AddressComponent res = new AddressComponent();
        res.setLongText(component.getLongName());
        res.setShortText(component.getShortName());
        res.setTypes(component.getTypes());
        return res;
    }

    //

    @Config("/services")
    @Component
    interface ServiceConfig {
        @Property("google-geocode-api.key")
        String getApiKey();
    }


    private class GoogleGeocodePlaceDetailsCommand extends UnirestCommand<GeocodePlaceByIdResponse> {

        static final String LANGUAGE = "en";

        private final String placeId;

        GoogleGeocodePlaceDetailsCommand(String placeId) {
            super(SERVICE_NAME, GeocodePlaceByIdResponse.class);
            this.placeId = placeId;
        }

        @Override
        protected GetRequest createRequest(String url, Path path) {
            GetRequest request = get(url, path)
                    .queryString("place_id", placeId)
                    .queryString("language", LANGUAGE)
                    .queryString("key", configuration.getApiKey());

            String channel = getChannel();
            if (StringUtils.isNotBlank(channel)) request = request.queryString("channel", channel);

            return request;
        }

        private String getChannel() {
            String channelId = AppContext.getChannelId();
            if (StringUtils.isBlank(channelId)) return null;

            return googleConfigurationService.getGoogleChannel(channelId);
        }

        @Override
        protected Path getPath() {
            return new Path("/geocode/json");
        }
    }
}