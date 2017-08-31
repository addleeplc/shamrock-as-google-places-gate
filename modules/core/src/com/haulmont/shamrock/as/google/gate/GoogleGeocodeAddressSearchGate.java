/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate;

import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.ServiceException;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.shamrock.address.Address;
import com.haulmont.shamrock.address.AddressSearchGate;
import com.haulmont.shamrock.address.GeocodeContext;
import com.haulmont.shamrock.address.Location;
import com.haulmont.shamrock.address.context.RefineContext;
import com.haulmont.shamrock.address.context.ReverseGeocodingContext;
import com.haulmont.shamrock.address.context.SearchBeneathContext;
import com.haulmont.shamrock.address.context.SearchContext;
import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.address.utils.GeoHelper;
import com.haulmont.shamrock.as.google.gate.dto.*;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressUtils;
import com.haulmont.shamrock.as.google.gate.utils.UnirestCommand;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.HttpRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Nikita Bozhko on 07.01.17.
 * Project Shamrock
 */
public class GoogleGeocodeAddressSearchGate implements AddressSearchGate {

    private static final Logger logger = LoggerFactory.getLogger(GoogleGeocodeAddressSearchGate.class);

    @Override
    public String getId() {
        return "google-geocode";
    }

    public List<Address> searchBeneath(SearchBeneathContext context) {
        throw new UnsupportedOperationException("Unsupported for " + getId() + " gate");
    }

    @Override
    public List<Address> search(SearchContext context) {
        GeocodingResponse response = new GoogleGeocodeSearchCommand(context).execute();
        GoogleApiStatus status = response.getStatus();
        if (status == GoogleApiStatus.UNKNOWN_ERROR || status == GoogleApiStatus.UNKNOWN) {
            throw new ServiceException(
                    ErrorCode.FAILED_DEPENDENCY,
                    String.format("GoogleGeocode Search API responds with non-OK status (status: %s)", status)
            );
        } else if (status == GoogleApiStatus.INVALID_REQUEST) {
            throw new ServiceException(
                    ErrorCode.FAILED_DEPENDENCY,
                    String.format("GoogleGeocode Search API responds with invalid request (status: %s)", status)
            );
        } else if (status == GoogleApiStatus.REQUEST_DENIED) {
            throw new ServiceException(
                    ErrorCode.FAILED_DEPENDENCY,
                    String.format("GoogleGeocode Search API responds with request denied (status: %s)", status)
            );
        } else if (status == GoogleApiStatus.OVER_QUERY_LIMIT) {
            throw new ServiceException(
                    ErrorCode.FAILED_DEPENDENCY,
                    String.format("GoogleGeocode Search API responds with over query limit (status: %s)", status)
            );
        } else if (status == GoogleApiStatus.ZERO_RESULTS) {
            return Collections.emptyList();
        } else {
            if (CollectionUtils.isNotEmpty(response.getResults())) {
                return convertSearchResponse(context, response);
            }
        }

        return Collections.emptyList();
    }

    private List<Address> convertSearchResponse(SearchContext context, GeocodingResponse response) {
        List<GeocodingResult> results = response.getResults();
        for (GeocodingResult o : results) {
            Map<String, AddressComponent> components = GoogleAddressUtils.convert(o.getAddressComponents());

            try {
                Address a = parseAddress(o.getFormattedAddress(), context.getCountry(), o.getGeometry(), components, o.getTypes());

                if (a != null) {
                    a.setId(String.format("%s|%s", getId(), null));
                    a.setRefined(true);

                    return Collections.singletonList(a);
                }
            } catch (Throwable e) {
                logger.warn("Failed to parse address", e);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public Address refine(RefineContext context) {
        return AddressHelper.convert(context.getAddress(), context.getRefineType());
    }

    @Override
    public Address geocode(GeocodeContext context) {
        Location location = context.getLocation();
        if (location != null && location.getLat() != null && location.getLon() != null) {
            if (StringUtils.isNotBlank(context.getAddress())) {
                Address a = geocodeByAddress(context);
                if (a != null && a.getAddressData() != null) {
                    Location aLocation = a.getAddressData().getLocation();
                    if (aLocation != null && aLocation.getLat() != null && aLocation.getLon() != null) {
                        double distance = GeoHelper.getGeoDistance(location.getLon(), location.getLat(), aLocation.getLon(), aLocation.getLat());
                        if (distance < getGateConfiguration().getDistanceThreshold()) {
                            return a;
                        } else {
                            return geocodeByLocation(context);
                        }
                    } else {
                        return geocodeByLocation(context);
                    }
                } else {
                    return geocodeByLocation(context);
                }
            } else {
                return geocodeByLocation(context);
            }
        } else {
            return geocodeByAddress(context);
        }
    }

    @Override
    public List<Address> reverseGeocode(ReverseGeocodingContext context) {
        throw new UnsupportedOperationException("Unsupported operation for " + getId() + " gate");
    }

    private Address geocodeByAddress(final GeocodeContext context) {
        GeocodingResponse response = new GoogleGeocodeGeocodeCommand(context).execute();
        GoogleApiStatus status = response.getStatus();
        if (status == GoogleApiStatus.UNKNOWN_ERROR || status == GoogleApiStatus.UNKNOWN) {
            throw new ServiceException(
                    ErrorCode.FAILED_DEPENDENCY,
                    String.format("GoogleGeocode Geocode API responds with non-OK status (status: %s)", status)
            );
        } else if (status == GoogleApiStatus.INVALID_REQUEST) {
            throw new ServiceException(
                    ErrorCode.FAILED_DEPENDENCY,
                    String.format("GoogleGeocode Geocode API responds with invalid request (status: %s)", status)
            );
        } else if (status == GoogleApiStatus.REQUEST_DENIED) {
            throw new ServiceException(
                    ErrorCode.FAILED_DEPENDENCY,
                    String.format("GoogleGeocode Geocode API responds with request denied (status: %s)", status)
            );
        } else if (status == GoogleApiStatus.OVER_QUERY_LIMIT) {
            throw new ServiceException(
                    ErrorCode.FAILED_DEPENDENCY,
                    String.format("GoogleGeocode Geocode API responds with over query limit (status: %s)", status)
            );
        } else if (status == GoogleApiStatus.ZERO_RESULTS) {
            return null;
        } else {
            if (CollectionUtils.isNotEmpty(response.getResults())) {
                return convertGeocodeResponse(context, response);
            }
        }

        return null;
    }

    private Address convertGeocodeResponse(GeocodeContext context, GeocodingResponse response) {
        List<GeocodingResult> results = response.getResults();
        for (GeocodingResult o : results) {
            Map<String, AddressComponent> components = GoogleAddressUtils.convert(o.getAddressComponents());

            try {
                Address a = parseAddress(o.getFormattedAddress(), context.getCountry(), o.getGeometry(), components, o.getTypes());

                if (a != null) {
                    a.setId(String.format("%s|%s", getId(), null));
                    a.setRefined(true);

                    return a;
                }
            } catch (Throwable e) {
                logger.warn("Failed to parse address", e);
            }
        }

        return null;
    }

    private Address geocodeByLocation(final GeocodeContext context) {
        GeocodingResponse response = new GoogleGeocodeGeocodeCommand(context).execute();
        GoogleApiStatus status = response.getStatus();
        if (status == GoogleApiStatus.UNKNOWN_ERROR || status == GoogleApiStatus.UNKNOWN) {
            throw new ServiceException(
                    ErrorCode.FAILED_DEPENDENCY,
                    String.format("GoogleGeocode Geocode API responds with non-OK status (status: %s)", status)
            );
        } else if (status == GoogleApiStatus.INVALID_REQUEST) {
            throw new ServiceException(
                    ErrorCode.FAILED_DEPENDENCY,
                    String.format("GoogleGeocode Geocode API responds with invalid request (status: %s)", status)
            );
        } else if (status == GoogleApiStatus.REQUEST_DENIED) {
            throw new ServiceException(
                    ErrorCode.FAILED_DEPENDENCY,
                    String.format("GoogleGeocode Geocode API responds with request denied (status: %s)", status)
            );
        } else if (status == GoogleApiStatus.OVER_QUERY_LIMIT) {
            throw new ServiceException(
                    ErrorCode.FAILED_DEPENDENCY,
                    String.format("GoogleGeocode Geocode API responds with over query limit (status: %s)", status)
            );
        } else if (status == GoogleApiStatus.ZERO_RESULTS) {
            return null;
        } else {
            if (CollectionUtils.isNotEmpty(response.getResults())) {
                return convertGeocodeResponse(context, response);
            }
        }

        return null;
    }

    private static Address parseAddress(String formattedAddress, String reqCountry,
                                        Geometry geometry,
                                        Map<String, AddressComponent> components,
                                        List<String> types) {

        try {
            return GoogleAddressUtils.parseAddress(formattedAddress, reqCountry, geometry, components, types);
        } catch (GoogleAddressUtils.AddressParseException e) {
            logger.debug(String.format("Failed to parse address '%s': %s", formattedAddress, e.getMessage()));
        }

        return null;
    }

    private static GateConfiguration getGateConfiguration() {
        return AppContext.getConfig().get(GateConfiguration.class);
    }

    private static class GoogleGeocodeSearchCommand extends UnirestCommand<GeocodingResponse> {
        private SearchContext context;

        public GoogleGeocodeSearchCommand(SearchContext context) {
            super("GoogleGeocode.Search", GeocodingResponse.class);
            this.context = context;
        }

        @Override
        protected BaseRequest createRequest(String url, Path path) {
            HttpRequest request = get(url, path)
                    .queryString("language", "en")
                    .queryString("key", getGateConfiguration().getSearchApiKey())
                    .queryString("address", context.getSearchString());

            if (StringUtils.isNotBlank(context.getCountry()) || StringUtils.isNotBlank(context.getCity())) {
                boolean f = true;

                StringBuilder buffer = new StringBuilder("");
                if (StringUtils.isNotBlank(context.getCountry())) {
                    buffer.append("country:").append(context.getCountry());
                    f = false;
                }

                if (StringUtils.isNotBlank(context.getCity())) {
                    if (!f) buffer.append("|");
                    buffer.append("locality:").append(context.getCity());
                    f = false;
                }

                request = request.queryString("components", buffer.toString());
            }

            return request;
        }

        @Override
        protected String getUrl() {
            return getGateConfiguration().getApiUrl();
        }

        @Override
        protected Path getPath() {
            return new Path("");
        }
    }

    private static class GoogleGeocodeGeocodeCommand extends UnirestCommand<GeocodingResponse> {
        private GeocodeContext context;

        public GoogleGeocodeGeocodeCommand(GeocodeContext context) {
            super("GoogleGeocode.Geocode", GeocodingResponse.class);
            this.context = context;
        }

        @Override
        protected BaseRequest createRequest(String url, Path path) {
            HttpRequest request = get(url, path)
                    .queryString("language", "en")
                    .queryString("key", getGateConfiguration().getSearchApiKey());

            Location location = context.getLocation();
            if (location != null && location.getLat() != null && location.getLon() != null) {
                request = request.queryString("latlng", String.format("%.6f,%.6f", location.getLat(), location.getLon()))
                        .queryString("location_type", "ROOFTOP")
                        .queryString("result_type", "street_address");
            } else {
                request = request.queryString("address", context.getAddress());
                if (StringUtils.isNotBlank(context.getCountry()) || StringUtils.isNotBlank(context.getCity())) {
                    boolean f = true;

                    StringBuilder buffer = new StringBuilder("");
                    if (StringUtils.isNotBlank(context.getCountry())) {
                        buffer.append("country:").append(context.getCountry());
                        f = false;
                    }

                    if (StringUtils.isNotBlank(context.getCity())) {
                        if (!f) buffer.append("|");
                        buffer.append("locality:").append(context.getCity());
                        f = false;
                    }

                    request = request.queryString("components", buffer.toString());
                }
            }

            return request;
        }

        @Override
        protected String getUrl() {
            return getGateConfiguration().getApiUrl();
        }

        @Override
        protected Path getPath() {
            return new Path("");
        }
    }
}
