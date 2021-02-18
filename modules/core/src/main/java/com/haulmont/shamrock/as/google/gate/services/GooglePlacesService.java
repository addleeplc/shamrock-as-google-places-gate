/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.services;

import com.google.common.geometry.S2LatLngRect;
import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.unirest.UnirestCommand;
import com.haulmont.shamrock.as.context.AutocompleteContext;
import com.haulmont.shamrock.as.contexts.GeoRegion;
import com.haulmont.shamrock.as.contexts.ReverseGeocodingContext;
import com.haulmont.shamrock.as.contexts.SearchContext;
import com.haulmont.shamrock.as.dto.CircularRegion;
import com.haulmont.shamrock.as.dto.LocationWithAccuracy;
import com.haulmont.shamrock.as.google.gate.ServiceConfiguration;
import com.haulmont.shamrock.as.google.gate.config.GoogleConfigurationService;
import com.haulmont.shamrock.as.google.gate.constants.GeometryConstants;
import com.haulmont.shamrock.as.google.gate.dto.Place;
import com.haulmont.shamrock.as.google.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.gate.dto.Prediction;
import com.haulmont.shamrock.as.google.gate.services.dto.google.ResponseStatus;
import com.haulmont.shamrock.as.google.gate.services.dto.google.places.FindPlaceResponse;
import com.haulmont.shamrock.as.google.gate.services.dto.google.places.PlaceDetailsResponse;
import com.haulmont.shamrock.as.google.gate.services.dto.google.places.PlacesResponse;
import com.haulmont.shamrock.as.google.gate.services.dto.google.places.PredictionsResponse;
import kong.unirest.GetRequest;
import org.apache.commons.lang.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GooglePlacesService {
    private static final String SERVICE_NAME = "google-places-api";

    @Inject
    private ServiceConfiguration configuration;

    @Inject
    private GoogleConfigurationService googleConfigurationService;

    //

    public List<Place> getPlaces(SearchContext context) {
        FindPlaceResponse response = new GooglePlacesTextSearchCommand(context).execute();

        ResponseStatus status = response.getStatus();
        return GoogleResponseUtils.checkResponse(status, response::getCandidates);
    }

    public List<Prediction> autocomplete(AutocompleteContext context) {
        PredictionsResponse response = new GooglePlacesAutocompleteCommand(context).execute();

        ResponseStatus status = response.getStatus();
        return GoogleResponseUtils.checkResponse(status,  () -> filter(response.getPredictions()));
    }

    private List<Prediction> filter(List<Prediction> predictions) {
        return predictions.stream().filter(p -> p.getTypes().contains("establishment") || p.getTypes().contains("street_address") || p.getTypes().contains("premise")).collect(Collectors.toList());
    }

    public List<Place> getPlaces(ReverseGeocodingContext context) {
        PlacesResponse response = new GooglePlacesNearbySearchCommand(context).execute();

        ResponseStatus status = response.getStatus();
        return GoogleResponseUtils.checkResponse(status, response::getResults);
    }

    public PlaceDetails getPlaceDetails(String id) {
        PlaceDetailsResponse response = new GooglePlaceDetailsCommand(id).execute();

        ResponseStatus status = response.getStatus();
        return GoogleResponseUtils.checkResponse(status, response::getResult);
    }

    private String getChannel() {
        String channelId = AppContext.getChannelId();
        if (org.apache.commons.lang3.StringUtils.isBlank(channelId)) return null;

        return googleConfigurationService.getGoogleChannel(channelId);
    }

    //

    class GooglePlacesTextSearchCommand extends UnirestCommand<FindPlaceResponse> {

        static final String LANGUAGE = "en";
        static final String INPUT_TYPE = "textquery";
        static final String FIELDS = "id,place_id,name,formatted_address,geometry/location,types";

        private SearchContext ctx;

        GooglePlacesTextSearchCommand(SearchContext ctx) {
            super(SERVICE_NAME, FindPlaceResponse.class);
            this.ctx = ctx;
        }


        @Override
        protected GetRequest createRequest(String url, Path path) {
            GetRequest request = get(url, path)
                    .queryString("language", LANGUAGE)
                    .queryString("key", getApiKey())
                    .queryString("input", ctx.getSearchString())
                    .queryString("inputtype", INPUT_TYPE)
                    .queryString("fields", FIELDS);

            if (StringUtils.isNotBlank(ctx.getCountry())) {
                request = request.queryString("region", ctx.getCountry());

                if (ctx.getCountry().equals("JE")) {
                    S2LatLngRect bound = GeometryConstants.JERSEY_POLYGON.getRectBound();
                    String locationbias = String.format(
                            "rectangle:%f,%f|%f,%f",
                            bound.lo().latDegrees(), bound.lo().lngDegrees(),
                            bound.hi().latDegrees(), bound.hi().lngDegrees()
                    );
                    request = request.queryString("locationbias", locationbias);
                }
            }

            String channel = getChannel();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(channel)) request = request.queryString("channel", channel);

            return request;
        }

        private String getApiKey() {
            String key = configuration.getGooglePlacesTextSearchApiKey();
            return StringUtils.isNotBlank(key) ? key : configuration.getGooglePlacesApiKey();
        }

        @Override
        protected String getUrl() {
            return configuration.getApiUrl();
        }

        @Override
        protected Path getPath() {
            return new Path("/place/findplacefromtext/json");
        }
    }

    class GooglePlacesAutocompleteCommand extends UnirestCommand<PredictionsResponse> {

        static final String LANGUAGE = "en";

        private AutocompleteContext ctx;

        GooglePlacesAutocompleteCommand(AutocompleteContext ctx) {
            super(SERVICE_NAME, PredictionsResponse.class);
            this.ctx = ctx;
        }


        @Override
        protected GetRequest createRequest(String url, Path path) {
            GetRequest request = get(url, path)
                    .queryString("language", LANGUAGE)
                    .queryString("key", getApiKey())
                    .queryString("input", ctx.getSearchString());

            LocationWithAccuracy origin = ctx.getOrigin();
            if (origin != null) {
                request = request.queryString("origin", String.format("%.6f,%.6f", origin.getLat(), origin.getLon()));
            }

            CircularRegion searchRegion = ctx.getSearchRegion();
            if (searchRegion != null) {
                request = request.queryString("location", String.format("%.6f,%.6f", searchRegion.getLat(), searchRegion.getLon()));
                request = request.queryString("radius", String.format("%.6f", searchRegion.getRadius()));
            } else if (origin != null) {
                request = request.queryString("location", String.format("%.6f,%.6f", origin.getLat(), origin.getLon()));
                request = request.queryString("radius", String.format("%.6f", 1000.0));
            }

            if (StringUtils.isNotBlank(ctx.getCountry())) {
                request = request.queryString("components", "country:" + StringUtils.lowerCase(ctx.getCountry()));
            }

            String channel = getChannel();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(channel)) request = request.queryString("channel", channel);

            return request;
        }

        private String getApiKey() {
            String key = configuration.getGooglePlacesAutocompleteApiKey();
            return StringUtils.isNotBlank(key) ? key : configuration.getGooglePlacesApiKey();
        }

        @Override
        protected String getUrl() {
            return configuration.getApiUrl();
        }

        @Override
        protected Path getPath() {
            return new Path("/place/autocomplete/json");
        }
    }

    class GooglePlaceDetailsCommand extends UnirestCommand<PlaceDetailsResponse> {

        static final String LANGUAGE = "en";
        static final String FIELDS = "id,place_id,name,formatted_address,geometry/location,types,address_components";

        private String placeId;

        GooglePlaceDetailsCommand(String placeId) {
            super(SERVICE_NAME, PlaceDetailsResponse.class);
            this.placeId = placeId;
        }

        @Override
        protected GetRequest createRequest(String url, Path path) {
            GetRequest request = get(url, path)
                    .queryString("placeid", placeId)
                    .queryString("language", LANGUAGE)
                    .queryString("key", getApiKey())
                    .queryString("fields", FIELDS);

            String channel = getChannel();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(channel)) request = request.queryString("channel", channel);

            return request;
        }

        private String getApiKey() {
            String key = configuration.getGooglePlacesDetailsApiKey();
            return StringUtils.isNotBlank(key) ? key : configuration.getGooglePlacesApiKey();
        }

        @Override
        protected String getUrl() {
            return configuration.getApiUrl();
        }

        @Override
        protected Path getPath() {
            return new Path("/place/details/json");
        }
    }

    class GooglePlacesNearbySearchCommand extends UnirestCommand<PlacesResponse> {

        static final String LANGUAGE = "en";

        private ReverseGeocodingContext context;

        GooglePlacesNearbySearchCommand(ReverseGeocodingContext context) {
            super(SERVICE_NAME, PlacesResponse.class);
            this.context = context;
        }

        @Override
        protected GetRequest createRequest(String url, Path path) {
            GeoRegion gr = context.getSearchRegion();

            GetRequest request = get(url, path)
                    .queryString("location", String.format("%.6f,%.6f", gr.getLatitude(), gr.getLongitude()))
                    .queryString("radius", String.format("%.6f", gr.getRadius()))
                    .queryString("language", LANGUAGE)
                    .queryString("key", getApiKey());

            String channel = getChannel();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(channel)) request = request.queryString("channel", channel);

            return request;
        }

        private String getApiKey() {
            String key = configuration.getGooglePlacesNearbySearchApiKey();
            return StringUtils.isNotBlank(key) ? key : configuration.getGooglePlacesApiKey();
        }

        @Override
        protected String getUrl() {
            return configuration.getApiUrl();
        }

        @Override
        protected Path getPath() {
            return new Path("/place/nearbysearch/json");
        }
    }
}
