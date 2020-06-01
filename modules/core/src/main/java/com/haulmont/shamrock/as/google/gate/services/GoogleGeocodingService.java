/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.services;

import com.haulmont.monaco.unirest.UnirestCommand;
import com.haulmont.shamrock.address.GeocodeContext;
import com.haulmont.shamrock.address.Location;
import com.haulmont.shamrock.address.context.SearchContext;
import com.haulmont.shamrock.as.google.gate.ServiceConfiguration;
import com.haulmont.shamrock.as.google.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.gate.services.dto.google.ResponseStatus;
import com.haulmont.shamrock.as.google.gate.services.dto.google.geocoding.GeocodingResponse;
import com.haulmont.shamrock.as.google.gate.services.dto.google.places.GeocodePlaceByIdResponse;
import com.haulmont.shamrock.as.google.gate.services.dto.google.places.PlaceDetailsResponse;
import kong.unirest.HttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;

import java.util.List;


@Component
public class GoogleGeocodingService {
    private static final String SERVICE_NAME = "google-geocoding-api";

    @Inject
    private ServiceConfiguration configuration;

    //

    public List<PlaceDetails> geocode(final SearchContext context) {
        GeocodingResponse response = new GoogleGeocodeCommand(context).execute();

        ResponseStatus status = response.getStatus();
        return GoogleResponseUtils.checkResponse(status, response::getResults);
    }

    public List<PlaceDetails> geocode(final GeocodeContext context) {
        GeocodingResponse response = new GoogleGeocodeCommand(context).execute();

        ResponseStatus status = response.getStatus();
        return GoogleResponseUtils.checkResponse(status, response::getResults);
    }

    public List<PlaceDetails> reverseGeocode(final GeocodeContext context) {
        GeocodingResponse response = new GoogleReverseGeocodeCommand(context).execute();

        ResponseStatus status = response.getStatus();
        return GoogleResponseUtils.checkResponse(status, response::getResults);
    }

    public PlaceDetails getPlaceDetails(String id) {
        GeocodePlaceByIdResponse response = new GoogleGeocodePlaceDetailsCommand(id).execute();

        ResponseStatus status = response.getStatus();
        return GoogleResponseUtils.checkResponse(status, () -> {
            List<PlaceDetails> results = response.getResults();
            if (results == null || results.isEmpty()) {
                return null;
            } else if (results.size() == 1) {
                return results.get(0);
            } else {
                throw new UnsupportedOperationException();
            }
        });
    }

    //

    private class GoogleGeocodeCommand extends UnirestCommand<GeocodingResponse> {
        static final String SERVICE_NAME = "google-geocoding-api";
        static final String LANGUAGE = "en";

        private Object context;

        GoogleGeocodeCommand(SearchContext context) {
            super(SERVICE_NAME, GeocodingResponse.class);
            this.context = context;
        }

        GoogleGeocodeCommand(GeocodeContext context) {
            super(SERVICE_NAME, GeocodingResponse.class);
            this.context = context;
        }

        @Override
        protected HttpRequest createRequest(String url, Path path) {
            String searchString = getSearchString();
            String country = getCountry();
            String city = getCity();

            HttpRequest request = get(url, path)
                    .queryString("language", LANGUAGE)
                    .queryString("key", configuration.getGoogleGeocodeApiKey())
                    .queryString("address", searchString);


            if (StringUtils.isNotBlank(country) || StringUtils.isNotBlank(city)) {
                boolean f = true;

                StringBuilder buffer = new StringBuilder("");
                if (StringUtils.isNotBlank(country)) {
                    buffer.append("country:").append(country);
                    f = false;
                }

                if (StringUtils.isNotBlank(city)) {
                    if (!f) buffer.append("|");
                    buffer.append("locality:").append(city);
                    f = false;
                }

                request = request.queryString("components", buffer.toString());
            }

            return request;
        }

        private String getSearchString() {
            if (context instanceof SearchContext) {
                return ((SearchContext) context).getSearchString();
            } else if (context instanceof GeocodeContext) {
                return ((GeocodeContext) context).getAddress();
            } else {
                throw new UnsupportedOperationException("Unknown context: " + context.getClass());
            }
        }

        private String getCountry() {
            if (context instanceof SearchContext) {
                return ((SearchContext) context).getCountry();
            } else if (context instanceof GeocodeContext) {
                return ((GeocodeContext) context).getCountry();
            } else {
                throw new UnsupportedOperationException("Unknown context: " + context.getClass());
            }
        }

        private String getCity() {
            if (context instanceof SearchContext) {
                return ((SearchContext) context).getCity();
            } else if (context instanceof GeocodeContext) {
                return ((GeocodeContext) context).getCity();
            } else {
                throw new UnsupportedOperationException("Unknown context: " + context.getClass());
            }
        }

        @Override
        protected String getUrl() {
            return configuration.getApiUrl();
        }

        @Override
        protected Path getPath() {
            return new Path("/geocode/json");
        }
    }

    private class GoogleReverseGeocodeCommand extends UnirestCommand<GeocodingResponse> {

        static final String LANGUAGE = "en";
        static final String GEOCODE_RESULT_TYPES = "street_address|premise|subpremise|park|point_of_interest";

        private GeocodeContext context;

        GoogleReverseGeocodeCommand(GeocodeContext context) {
            super(SERVICE_NAME, GeocodingResponse.class);
            this.context = context;
        }

        @Override
        protected HttpRequest createRequest(String url, Path path) {
            HttpRequest request = get(url, path)
                    .queryString("language", LANGUAGE)
                    .queryString("key", configuration.getGoogleGeocodeApiKey());

            Location location = context.getLocation();
            request = request.queryString("latlng", String.format("%.6f,%.6f", location.getLat(), location.getLon()))
                    .queryString("location_type", "ROOFTOP")
                    .queryString("result_type", GEOCODE_RESULT_TYPES);

            if (StringUtils.isNotBlank(context.getAddress()))
                request = request.queryString("address", context.getAddress())
                        .queryString("location_type", "ROOFTOP")
                        .queryString("result_type", GEOCODE_RESULT_TYPES);

            return request;
        }

        @Override
        protected String getUrl() {
            return configuration.getApiUrl();
        }

        @Override
        protected Path getPath() {
            return new Path("/geocode/json");
        }
    }

    class GoogleGeocodePlaceDetailsCommand extends UnirestCommand<GeocodePlaceByIdResponse> {

        static final String LANGUAGE = "en";

        private String placeId;

        GoogleGeocodePlaceDetailsCommand(String placeId) {
            super(SERVICE_NAME, GeocodePlaceByIdResponse.class);
            this.placeId = placeId;
        }

        @Override
        protected HttpRequest createRequest(String url, Path path) {
            return get(url, path)
                    .queryString("place_id", placeId)
                    .queryString("language", LANGUAGE)
                    .queryString("key", configuration.getGooglePlacesApiKey());
        }

        @Override
        protected String getUrl() {
            return configuration.getApiUrl();
        }

        @Override
        protected Path getPath() {
            return new Path("/geocode/json");
        }
    }
}
