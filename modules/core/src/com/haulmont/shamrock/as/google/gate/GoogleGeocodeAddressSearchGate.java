/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate;

import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.ServiceException;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.monaco.unirest.UnirestCommand;
import com.haulmont.shamrock.address.*;
import com.haulmont.shamrock.address.Location;
import com.haulmont.shamrock.address.context.*;
import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.address.utils.GeoHelper;
import com.haulmont.shamrock.as.google.gate.dto.*;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressUtils;
import com.haulmont.shamrock.geo.PostcodeHelper;
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
        long ts = System.currentTimeMillis();

        final List<Address> res;

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
            res = Collections.emptyList();
        } else {
            if (CollectionUtils.isNotEmpty(response.getResults())) {
                res = convertSearchResponse(response);
            } else {
                res = Collections.emptyList();
            }
        }

        logger.debug("Search address by text (text: '{}', resSize: {}) ({} ms)'", context.getSearchString(), res.size(), System.currentTimeMillis() - ts);

        return res;
    }

    private List<Address> convertSearchResponse(GeocodingResponse response) {
        List<GeocodingResult> results = response.getResults();
        for (GeocodingResult o : results) {
            Map<String, AddressComponent> components = GoogleAddressUtils.convert(o.getAddressComponents());

            try {
                Address a = parseAddress(o.getFormattedAddress(), o.getGeometry(), components, o.getTypes());

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
        return doRefine(context);
    }

    @Override
    public Address geocode(GeocodeContext context) {
        long ts = System.currentTimeMillis();

        Location loc = context.getLocation();
        if (loc != null && loc.getLat() != null && loc.getLon() != null) {
            final Address res;

            if (StringUtils.isNotBlank(context.getAddress())) {
                Address a = geocodeByAddress(context);
                if (a != null && a.getAddressData() != null) {
                    Location aLocation = a.getAddressData().getLocation();
                    if (aLocation != null && aLocation.getLat() != null && aLocation.getLon() != null) {
                        double distance = GeoHelper.getGeoDistance(loc.getLon(), loc.getLat(), aLocation.getLon(), aLocation.getLat());
                        if (distance < getGateConfiguration().getDistanceThreshold()) {
                            res = a;
                        } else {
                            res = geocodeByLocation(context);
                        }
                    } else {
                        res = geocodeByLocation(context);
                    }
                } else {
                    res = geocodeByLocation(context);
                }
            } else {
                res = geocodeByLocation(context);
            }

            logger.debug("Geocode address by location (loc: {},{}, res: '{}') ({} ms)", loc.getLat(), loc.getLon(), res != null ? res.getAddressData().getFormattedAddress(): "N/A", System.currentTimeMillis() - ts);

            return res;
        } else {
            final Address res = geocodeByAddress(context);

            logger.debug("Geocode address by text (text: '{}', res: '{}') ({} ms)", context.getAddress(), res != null ? res.getAddressData().getFormattedAddress(): "N/A", System.currentTimeMillis() - ts);

            return res;
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
                Address address = convertGeocodeResponse(response);

                if (address != null) {
                    AddressComponents addressComponents = address.getAddressData().getAddressComponents();
                    if (StringUtils.isNotBlank(addressComponents.getPostcode())) {
                        if (StringUtils.equalsIgnoreCase(context.getCountry(), "GB") || StringUtils.equalsIgnoreCase(addressComponents.getCountry(), "GB")) {
                            String postcode = context.getPostcode();
                            if (StringUtils.isBlank(postcode))
                                postcode = PostcodeHelper.parsePostcode(context.getAddress());

                            if (StringUtils.isNotBlank(postcode) && StringUtils.isNotBlank(addressComponents.getPostcode())) {
                                String postcodeArea = postcode.substring(0, postcode.length() - 2);

                                if (!StringUtils.startsWithIgnoreCase(addressComponents.getPostcode().replace(" ", ""), postcodeArea.replace(" ", "")))
                                    return null;
                            }
                        }
                    }
                }

                return address;
            }
        }

        return null;
    }

    private Address convertGeocodeResponse(GeocodingResponse response) {
        List<GeocodingResult> results = response.getResults();
        for (GeocodingResult o : results) {
            Map<String, AddressComponent> components = GoogleAddressUtils.convert(o.getAddressComponents());

            try {
                Address a = parseAddress(o.getFormattedAddress(), o.getGeometry(), components, o.getTypes());

                if (a != null) {
                    RefineContext ctx = new RefineContext();
                    ctx.setAddress(a);
                    ctx.setRefineType(RefineType.DEFAULT);

                    Address refined = doRefine(ctx);
                    if (refined != null) {
                        a = refined;
                    } else {
                        a.setId(String.format("%s|%s", getId(), null));
                        a.setRefined(true);
                    }

                    return a;
                }
            } catch (Throwable e) {
                logger.warn("Failed to parse address", e);
            }
        }

        return null;
    }

    private Address doRefine(RefineContext ctx) {
        if (ctx.getAddress().isRefined()) {
            return AddressHelper.convert(ctx.getAddress(), ctx.getRefineType());
        } else {
            try {
                Address a = ctx.getAddress();

                String id = AddressHelper.getAddressId(a);
                if (id == null)
                    return null;

                PlaceDetailsResponse placeDetailsResponse = new GoogleGeocodeRefineCommand(id).execute();

                GoogleApiStatus status = placeDetailsResponse.getStatus();
                if (status == GoogleApiStatus.UNKNOWN_ERROR || status == GoogleApiStatus.UNKNOWN) {
                    throw new ServiceException(
                            ErrorCode.FAILED_DEPENDENCY,
                            String.format("GooglePlaces Reverse Geocode API responds with non-OK status (status: %s)", status)
                    );
                } else if (status == GoogleApiStatus.INVALID_REQUEST) {
                    throw new ServiceException(
                            ErrorCode.FAILED_DEPENDENCY,
                            String.format("GooglePlaces Reverse Geocode API responds with invalid request (status: %s)", status)
                    );
                } else if (status == GoogleApiStatus.REQUEST_DENIED) {
                    throw new ServiceException(
                            ErrorCode.FAILED_DEPENDENCY,
                            String.format("GooglePlaces Reverse Geocode API responds with request denied (status: %s)", status)
                    );
                } else if (status == GoogleApiStatus.OVER_QUERY_LIMIT) {
                    throw new ServiceException(
                            ErrorCode.FAILED_DEPENDENCY,
                            String.format("GooglePlaces Reverse Geocode API responds with over query limit (status: %s)", status)
                    );
                } else if (status == GoogleApiStatus.ZERO_RESULTS) {
                    return null;
                } else {
                    if (placeDetailsResponse.getResult() == null) {
                        return null;
                    } else {
                        Address address = convertRefineResult(placeDetailsResponse);
                        if (address != null) {
                            logger.info(
                                    String.format(
                                            "Refine address '%s/%s' (%s, %s), result: %s",
                                            a.getId(), a.getAddressData().getFormattedAddress(),
                                            ctx.getRefineType().name(), ctx.getAddress().getAddressData().getAddressComponents().getCountry(),
                                            address.getAddressData().getFormattedAddress()
                                    )
                            );
                        }

                        return address;
                    }
                }
            } catch (ServiceException e) {
                throw e;
            } catch (Throwable t) {
                throw new ServiceException(ErrorCode.SERVER_ERROR, "Unknown error", t);
            }
        }
    }

    private Address convertRefineResult(PlaceDetailsResponse response) {
        PlaceDetailsResult details = response.getResult();
        Map<String, AddressComponent> components = GoogleAddressUtils.convert(details.getAddressComponents());

        try {
            Address res = parseAddress(details.getFormattedAddress(), details.getGeometry(), components, details.getTypes());

            if (res != null) {
                res.setId(String.format("%s|%s", getId(), details.getId()));
            } else {
                return null;
            }

            res.setRefined(true);

            GoogleAddressUtils.assignPlaceDetails(res, details);

            return res;
        } catch (Throwable e) {
            throw new ServiceException(ErrorCode.SERVER_ERROR, "", e);
        }
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
                return convertGeocodeResponse(response);
            }
        }

        return null;
    }

    private static Address parseAddress(String formattedAddress, Geometry geometry, Map<String, AddressComponent> components, List<String> types) {
        try {
            return GoogleAddressUtils.parseAddress(formattedAddress, geometry, components, types);
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
            HttpRequest request = get(url, new Path("/"))
                    .queryString("language", "en")
                    .queryString("key", getGateConfiguration().getGoogleGeocodeApiKey())
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
            return new Path("/geocode");
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
            HttpRequest request = get(url, new Path(""))
                    .queryString("language", "en")
                    .queryString("key", getGateConfiguration().getGoogleGeocodeApiKey());

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
            return new Path("/geocode");
        }
    }

    private static class GoogleGeocodeRefineCommand extends UnirestCommand<PlaceDetailsResponse> {
        private String placeId;

        public GoogleGeocodeRefineCommand(String placeId) {
            super("GoogleGeocode.Refine", PlaceDetailsResponse.class);
            this.placeId = placeId;
        }

        @Override
        protected BaseRequest createRequest(String url, Path path) {
            return get(url, path)
                    .queryString("placeid", placeId)
                    .queryString("language", "en")
                    .queryString("key", getGateConfiguration().getGoogleGeocodeApiKey());
        }

        @Override
        protected String getUrl() {
            return getGateConfiguration().getApiUrl();
        }

        @Override
        protected Path getPath() {
            return new Path("/details/json");
        }
    }
}
