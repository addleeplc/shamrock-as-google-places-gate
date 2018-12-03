/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.services;

import com.haulmont.monaco.unirest.UnirestCommand;
import com.haulmont.shamrock.address.context.GeoRegion;
import com.haulmont.shamrock.address.context.ReverseGeocodingContext;
import com.haulmont.shamrock.address.context.SearchContext;
import com.haulmont.shamrock.as.google.gate.ServiceConfiguration;
import com.haulmont.shamrock.as.google.gate.dto.*;
import com.haulmont.shamrock.as.google.gate.services.dto.google.ResponseStatus;
import com.haulmont.shamrock.as.google.gate.services.dto.google.places.FindPlaceResponse;
import com.haulmont.shamrock.as.google.gate.services.dto.google.places.PlaceDetailsResponse;
import com.haulmont.shamrock.as.google.gate.services.dto.google.places.PlacesResponse;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.HttpRequest;
import org.apache.commons.lang.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;

import java.util.List;

@Component
public class GooglePlacesService {
    private static final String SERVICE_NAME = "google-places-api";

    @Inject
    private ServiceConfiguration configuration;

    //

    public List<Place> getPlaces(SearchContext context) {
        FindPlaceResponse response = new GooglePlacesTextSearchCommand(context).execute();

        ResponseStatus status = response.getStatus();
        return GoogleResponseUtils.checkResponse(status, response::getCandidates);
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
        protected BaseRequest createRequest(String url, Path path) {
            HttpRequest request = get(url, path)
                    .queryString("language", LANGUAGE)
                    .queryString("key", configuration.getGooglePlacesApiKey())
                    .queryString("input", ctx.getSearchString())
                    .queryString("inputtype", INPUT_TYPE)
                    .queryString("fields", FIELDS);

            if (StringUtils.isNotBlank(ctx.getCountry()))
                request = request.queryString("region", ctx.getCountry());

            return request;
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

    class GooglePlaceDetailsCommand extends UnirestCommand<PlaceDetailsResponse> {

        static final String LANGUAGE = "en";
        static final String FIELDS = "id,place_id,name,formatted_address,geometry/location,types,address_components";

        private String placeId;

        GooglePlaceDetailsCommand(String placeId) {
            super(SERVICE_NAME, PlaceDetailsResponse.class);
            this.placeId = placeId;
        }

        @Override
        protected BaseRequest createRequest(String url, Path path) {
            return get(url, path)
                    .queryString("placeid", placeId)
                    .queryString("language", LANGUAGE)
                    .queryString("key", configuration.getGooglePlacesApiKey())
                    .queryString("fields", FIELDS);
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
        protected BaseRequest createRequest(String url, Path path) {
            GeoRegion gr = context.getSearchRegion();

            return get(url, path)
                    .queryString("location", String.format("%.6f,%.6f", gr.getLatitude(), gr.getLongitude()))
                    .queryString("radius", String.format("%.6f", gr.getRadius()))
                    .queryString("language", LANGUAGE)
                    .queryString("key", configuration.getGooglePlacesApiKey());
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
