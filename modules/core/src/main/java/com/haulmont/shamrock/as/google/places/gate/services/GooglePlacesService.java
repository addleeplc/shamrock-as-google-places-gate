/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.services;

import com.google.common.geometry.S2LatLngRect;
import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.config.annotations.Config;
import com.haulmont.monaco.config.annotations.Property;
import com.haulmont.monaco.unirest.UnirestCommand;
import com.haulmont.shamrock.as.context.AutocompleteContext;
import com.haulmont.shamrock.as.contexts.GeoRegion;
import com.haulmont.shamrock.as.contexts.ReverseGeocodingContext;
import com.haulmont.shamrock.as.contexts.SearchContext;
import com.haulmont.shamrock.as.dto.CircularRegion;
import com.haulmont.shamrock.as.dto.LocationWithAccuracy;
import com.haulmont.shamrock.as.google.places.gate.config.GoogleConfigurationService;
import com.haulmont.shamrock.as.google.places.gate.constants.GeometryConstants;
import com.haulmont.shamrock.as.google.places.gate.dto.Place;
import com.haulmont.shamrock.as.google.places.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.ResponseStatus;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.*;
import kong.unirest.GetRequest;
import kong.unirest.RequestBodyEntity;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GooglePlacesService {
    private static final String SERVICE_NAME = "google-places-api";

    @Inject
    private ServiceConfig configuration;

    @Inject
    private GoogleConfigurationService googleConfigurationService;

    //

    public List<Place> getPlaces(SearchContext context) {
        FindPlaceResponse response = new GooglePlacesTextSearchCommand(context).execute();

        ResponseStatus status = response.getStatus();
        return GoogleResponseUtils.checkResponse(status, response::getPlaces);
    }

    public List<PlacePrediction> autocomplete(AutocompleteContext context) {
        AutocompleteResponse response = new GooglePlacesAutocompleteCommand(context).execute();

        ResponseStatus status = response.getStatus();
        return GoogleResponseUtils.checkResponse(status, () -> filter(response.getSuggestions()));
    }

    public List<PlaceDetails> getPlaces(GeoRegion context) {
        PlacesResponse response = new GooglePlacesNearbySearchCommand(context).execute();

        ResponseStatus status = response.getStatus();
        return GoogleResponseUtils.checkResponse(status, response::getPlaces);
    }

    public PlaceDetails getPlaceDetails(String id) {
        PlaceDetailsResponse response = new GooglePlaceDetailsCommand(id).execute();

        if (GoogleResponseUtils.isResponseOk(response.getStatus()))
            return response;
        else
            return null;
    }

    private List<PlacePrediction> filter(List<Suggestion> suggestions) {
        return suggestions.stream().map(Suggestion::getPlacePrediction).filter(p -> {
            List<String> types = p.getTypes();
            return types != null && (types.contains("establishment") || types.contains("street_address") || types.contains("premise"));
        }).collect(Collectors.toList());
    }

    private String getChannel() {
        String channelId = AppContext.getChannelId();
        if (StringUtils.isBlank(channelId)) return null;

        return googleConfigurationService.getGoogleChannel(channelId);
    }

    //

    @Config("/services")
    @Component
    interface ServiceConfig {
        @Property("google-places-api.key")
        String getApiKey();
    }

    class GooglePlacesTextSearchCommand extends UnirestCommand<FindPlaceResponse> {
        private static final String LANGUAGE = "en";
        private static final String FIELDS = "places.displayName.text,places.formattedAddress,places.id,places.location,places.types";

        private final SearchContext ctx;

        GooglePlacesTextSearchCommand(SearchContext ctx) {
            super(SERVICE_NAME, FindPlaceResponse.class);
            this.ctx = ctx;
        }

        @Override
        protected RequestBodyEntity createRequest(String url, Path path) {
            SearchTextRequest searchTextRequest = new SearchTextRequest();
            searchTextRequest.setLanguageCode(LANGUAGE);
            searchTextRequest.setTextQuery(ctx.getSearchString());
            searchTextRequest.setRegionCode(ctx.getCountry());

            if (ctx.getCountry() != null && ctx.getCountry().equals("JE")) {
                S2LatLngRect bound = GeometryConstants.JERSEY_POLYGON.getRectBound();
                Viewport rectangle = new Viewport(new LatLng(bound.lo().latDegrees(), bound.lo().lngDegrees()), new LatLng(bound.hi().latDegrees(), bound.hi().lngDegrees()));
                searchTextRequest.setLocationBias(new Geometry(rectangle));
            }

            return post(url, path)
                    .header("content-type", "application/json")
                    .header("X-Goog-Api-Key", configuration.getApiKey())
                    .header("X-Goog-FieldMask", FIELDS)
                    .header("X-Channel-Id", getChannel())
                    .body(searchTextRequest);
        }

        @Override
        protected Path getPath() {
            return new Path("/places:searchText");
        }
    }

    class GooglePlacesAutocompleteCommand extends UnirestCommand<AutocompleteResponse> {
        private static final String LANGUAGE = "en";
        private static final String FIELDS = "suggestions.placePrediction.placeId,suggestions.placePrediction.text.text,suggestions.placePrediction.types";

        private final AutocompleteContext ctx;

        GooglePlacesAutocompleteCommand(AutocompleteContext ctx) {
            super(SERVICE_NAME, AutocompleteResponse.class);
            this.ctx = ctx;
        }


        @Override
        protected RequestBodyEntity createRequest(String url, Path path) {
            PlacesAutocompleteRequest placesAutocompleteRequest = new PlacesAutocompleteRequest();
            placesAutocompleteRequest.setLanguageCode(LANGUAGE);
            placesAutocompleteRequest.setInput(ctx.getSearchString());

            LocationWithAccuracy origin = ctx.getOrigin();
            if (origin != null)
                placesAutocompleteRequest.setOrigin(new LatLng(origin.getLat(), origin.getLon()));

            CircularRegion searchRegion = ctx.getSearchRegion();
            Circle region;
            if (searchRegion != null)
                region = new Circle(new LatLng(searchRegion.getLat(), searchRegion.getLon()), searchRegion.getRadius());
            else if (origin != null)
                region = new Circle(new LatLng(origin.getLat(), origin.getLon()), 1000.0);
            else
                region = null;

            if (region != null)
                placesAutocompleteRequest.setLocationBias(new Geometry(region));

            if (StringUtils.isNotBlank(ctx.getCountry()))
                placesAutocompleteRequest.setIncludedRegionCodes(new String[]{ctx.getCountry().toLowerCase()});

            return post(url, path)
                    .header("content-type", "application/json")
                    .header("X-Goog-Api-Key", configuration.getApiKey())
                    .header("X-Goog-FieldMask", FIELDS)
                    .header("X-Channel-Id", getChannel())
                    .body(placesAutocompleteRequest);
        }

        @Override
        protected Path getPath() {
            return new Path("/places:autocomplete");
        }
    }

    class GooglePlaceDetailsCommand extends UnirestCommand<PlaceDetailsResponse> {

        static final String LANGUAGE = "en";
        static final String FIELDS = "id,displayName,formattedAddress,location,types,addressComponents";

        private final String placeId;

        GooglePlaceDetailsCommand(String placeId) {
            super(SERVICE_NAME, PlaceDetailsResponse.class);
            this.placeId = placeId;
        }

        @Override
        protected GetRequest createRequest(String url, Path path) {
            return get(url, path)
                    .queryString("languageCode", LANGUAGE)
                    .header("X-Goog-Api-Key", configuration.getApiKey())
                    .header("X-Goog-FieldMask", FIELDS)
                    .header("X-Channel-Id", getChannel());
        }

        @Override
        protected Path getPath() {
            return new Path("/places/{place_id}", Map.of("place_id", placeId));
        }
    }

    class GooglePlacesNearbySearchCommand extends UnirestCommand<PlacesResponse> {

        static final String LANGUAGE = "en";
        static final String FIELDS = "places.id,places.displayName,places.formattedAddress,places.location,places.types,places.addressComponents";


        private final GeoRegion gr;

        GooglePlacesNearbySearchCommand(GeoRegion region) {
            super(SERVICE_NAME, PlacesResponse.class);
            this.gr = region;
        }

        @Override
        protected RequestBodyEntity createRequest(String url, Path path) {
            SearchNearbyRequest searchNearbyRequest = new SearchNearbyRequest();
            searchNearbyRequest.setLanguageCode(LANGUAGE);
            LocationRestriction locationRestriction = new LocationRestriction(new Circle(new LatLng(gr.getLatitude(), gr.getLongitude()), gr.getRadius()));
            searchNearbyRequest.setLocationRestriction(locationRestriction);

            return post(url, path)
                    .header("content-type", "application/json")
                    .header("X-Goog-Api-Key", configuration.getApiKey())
                    .header("X-Goog-FieldMask", FIELDS)
                    .header("X-Channel-Id", getChannel())
                    .body(searchNearbyRequest);
        }

        @Override
        protected Path getPath() {
            return new Path("/places:searchNearby");
        }
    }
}