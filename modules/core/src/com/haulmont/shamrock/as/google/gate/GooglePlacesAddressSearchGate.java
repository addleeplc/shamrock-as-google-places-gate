/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate;

import com.google.common.collect.Lists;
import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.ServiceException;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.monaco.unirest.UnirestCommand;
import com.haulmont.shamrock.address.*;
import com.haulmont.shamrock.address.Location;
import com.haulmont.shamrock.address.context.*;
import com.haulmont.shamrock.address.gis.GISUtils;
import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.address.utils.GeoHelper;
import com.haulmont.shamrock.address.utils.StringHelper;
import com.haulmont.shamrock.as.google.gate.dto.*;
import com.haulmont.shamrock.as.google.gate.utils.CityGeometry;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressSearchUtils;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressUtils;
import com.haulmont.shamrock.geo.PostcodeHelper;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.HttpRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class GooglePlacesAddressSearchGate implements AddressSearchGate {

    private static final Logger logger = LoggerFactory.getLogger(GooglePlacesAddressSearchGate.class);

    @Override
    public String getId() {
        return "google-places";
    }

    public List<Address> searchBeneath(SearchBeneathContext context) {
        throw new UnsupportedOperationException("Unsupported for " + getId() + " gate");
    }

    @Override
    public List<Address> search(SearchContext context) {
        String searchString = context.getSearchString();
        if (StringUtils.isEmpty(searchString)) return null;

        long ts = System.currentTimeMillis();

        List<Address> addresses;

        String postcode = PostcodeHelper.parsePostcode(context.getSearchString());
        if (postcode == null)
            postcode = context.getPostcode();

        boolean partialPostcode = PostcodeHelper.parsePostcode(postcode, false) == null;
        if (StringUtils.isNotBlank(postcode) && !partialPostcode &&
                context.getSearchString().equalsIgnoreCase(postcode)) {
            addresses = doSearch(context);
        } else {
            if (StringUtils.isNotBlank(context.getCity()) && !StringUtils.containsIgnoreCase(searchString, context.getCity())) {
                SearchContext temp = GoogleAddressSearchUtils.clone(context);
                temp.setCity(context.getCity());
                temp.setSearchString(searchString + ", " + context.getCity());

                addresses = doSearch(temp);
            } else {
                //First step
                addresses = doSearch(context);

                if (CollectionUtils.isEmpty(addresses)) {
                    SearchContext temp = GoogleAddressSearchUtils.clone(context);
                    temp.setCity(context.getPreferredCity());
                    temp.setCountry(context.getPreferredCountry());
                    temp.setSearchString(searchString + ", " + context.getPreferredCity());

                    List<Address> pcAddresses = doSearch(temp);
                    addresses.addAll(pcAddresses);
                }
            }
        }

        final List<Address> res = GoogleAddressSearchUtils.filter(addresses);

        logger.debug("Search address by text (text: '{}', resSize: {}) ({} ms)'", context.getSearchString(), res.size(), System.currentTimeMillis() - ts);

        return res;
    }

    private List<Address> doSearch(final SearchContext context) {
        int maxSearchPages = getGateConfiguration().getMaxSearchPages();

        List<PlacesResult> placesResults = new ArrayList<>();
        PlacesResponse previous = null;
        for (int i = 0; i < maxSearchPages; ++i) {
            try {
                if (i == 0) {
                    previous = new GooglePlacesSearchCommand(context).execute();
                } else if (previous != null && StringUtils.isNotBlank(previous.getNextPageToken())) {
                    previous = new GooglePlacesSearchNextPageCommand(previous.getNextPageToken()).execute();
                } else {
                    break;
                }

                GoogleApiStatus status = previous.getStatus();
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
                    return Collections.emptyList();
                } else {
                    if (CollectionUtils.isNotEmpty(previous.getResults())) {
                        placesResults.addAll(previous.getResults());
                    }
                }
            } catch (Throwable e) {
                logger.warn("Failed to search address", e);
            }
        }

        if (CollectionUtils.isEmpty(placesResults))
            return Collections.emptyList();

        return placesResults.parallelStream()
                .map(r -> convertSearchResult(context, r))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Address convertSearchResult(SearchContext context, PlacesResult result) {
        try {
            Address a = new Address();

            AddressData ad = new AddressData();
            AddressComponents ac = new AddressComponents();

            a.setId(String.format("%s|%s", getId(), result.getPlaceId()));
            ad.setFormattedAddress(result.getName() + ", " + result.getVicinity());

            if (context.getCountry() != null) {
                ac.setCountry(context.getCountry());
            }

            ad.setAddressComponents(ac);
            a.setAddressData(ad);

            RefineContext refineContext = new RefineContext();
            refineContext.setAddress(a);
            refineContext.setRefineType(RefineType.DEFAULT);
            a = refine(refineContext);
            if (a != null) {
                return a;
            }
        } catch (Throwable e) {
            logger.warn("Failed to parse address", e);
        }

        return null;
    }

    @Override
    public Address refine(RefineContext context) throws RuntimeException {
        return doRefine(context);
    }

    private Address doRefine(RefineContext context) {
        if (context.getAddress().isRefined()) {
            return AddressHelper.convert(context.getAddress(), context.getRefineType());
        } else {
            try {
                Address a = context.getAddress();

                String id = AddressHelper.getAddressId(a);
                if (id == null)
                    return null;

                PlaceDetailsResponse placeDetailsResponse = new GooglePlacesRefineCommand(id).execute();

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
                                            context.getRefineType().name(), getRequestedCountry(context),
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

    @Override
    public Address geocode(GeocodeContext context) {
        throw new UnsupportedOperationException("Unsupported for " + getId() + " gate");
    }

    @Override
    public List<Address> reverseGeocode(ReverseGeocodingContext context) {
        List<Address> res;

        long ts = System.currentTimeMillis();

        try {
            PlacesResponse placesResponse = new GooglePlacesReverseGeocodeCommand(context).execute();

            GoogleApiStatus status = placesResponse.getStatus();
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
                res = Collections.emptyList();
            } else {
                if (CollectionUtils.isEmpty(placesResponse.getResults())) {
                    res = Collections.emptyList();
                } else {
                    res = Lists.partition(placesResponse.getResults(), 5)
                            .parallelStream()
                            .map(pr -> convertReverseGeocodeResults(context, pr))
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                }
            }

            Address o = res.isEmpty() ? null : res.get(0);

            logger.debug("Reverse geocode address by location (loc: {},{}, res: '{}', resSize: {}) ({} ms)", context.getSearchRegion().getLatitude(), context.getSearchRegion().getLongitude(), o != null ? o.getAddressData().getFormattedAddress(): "N/A", res.size(), System.currentTimeMillis() - ts);

            return res;
        } catch (ServiceException e) {
            throw e;
        } catch (Throwable t) {
            throw new ServiceException(ErrorCode.SERVER_ERROR, "Unknown error", t);
        }
    }

    private List<Address> convertReverseGeocodeResults(ReverseGeocodingContext context, List<PlacesResult> results) {
        List<Address> res = new ArrayList<>();
        for (PlacesResult o : results) {
            try {
                if (o.getTypes().size() == 1 && o.getTypes().contains("route")) continue;

                Address tmp = new Address();

                String name = StringHelper.convertToAscii(o.getName().trim());
                tmp.setId(getId() + "|" + o.getPlaceId());
                AddressData data = new AddressData();
                data.setFormattedAddress(name + ", " + o.getVicinity());
                tmp.setAddressData(data);

                AddressComponents ac = new AddressComponents();
                Geometry geometry = o.getGeometry();
                if (geometry != null && geometry.getLocation() != null &&
                        geometry.getLocation().getLat() != null && geometry.getLocation().getLng() != null) {
                    for (CityGeometry city : GeometryConstants.CITIES) {
                        if (GISUtils.isInsideGeometry(city, geometry.getLocation().getLat(), geometry.getLocation().getLng())) {
                            ac.setCountry(city.getCountry());
                        }
                    }
                }
                data.setAddressComponents(ac);

                RefineContext refineContext = new RefineContext();
                refineContext.setAddress(tmp);
                refineContext.setRefineType(RefineType.DEFAULT);

                Address a = doRefine(refineContext);
                if (isAddressCoordinatesBlank(a)) {
                    Location l = a.getAddressData().getLocation();
                    double distance = GeoHelper.getGeoDistance(l.getLon(), l.getLat(), context.getSearchRegion().getLongitude(), context.getSearchRegion().getLatitude());
                    if (distance <= context.getSearchRegion().getRadius()) {
                        a.setDistance(distance);
                        res.add(a);
                    }
                }
            } catch (Throwable e) {
                logger.warn("Fail to parse address: " + (o.getName() + ", " + o.getVicinity()), e);
            }
        }

        logger.info(
                String.format(
                        "Search addresses near (%s, %s), radius %s, found: %s",
                        context.getSearchRegion().getLatitude(), context.getSearchRegion().getLongitude(),
                        context.getSearchRegion().getRadius(), res.size()
                )
        );

        return res;
    }

    private String getRequestedCountry(RefineContext context) {
        String reqCountry = null;
        if (context.getAddress().getAddressData() != null && context.getAddress().getAddressData().getAddressComponents() != null)
            reqCountry = context.getAddress().getAddressData().getAddressComponents().getCountry();
        return reqCountry;
    }

    private boolean isAddressCoordinatesBlank(Address address) {
        if (address == null)
            throw new IllegalArgumentException("Address should be not null");

        return address.getAddressData() != null && address.getAddressData().getLocation() != null
                && address.getAddressData().getLocation().getLat() != null &&
                address.getAddressData().getLocation().getLon() != null;
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

    private static class GooglePlacesSearchCommand extends UnirestCommand<PlacesResponse> {
        private SearchContext ctx;

        public GooglePlacesSearchCommand(SearchContext ctx) {
            super("GooglePlaces.Search", PlacesResponse.class);
            this.ctx = ctx;
        }


        @Override
        protected BaseRequest createRequest(String url, Path path) {
            HttpRequest request = get(url, path)
                    .queryString("language", "en")
                    .queryString("key", getGateConfiguration().getGooglePlacesApiKey())
                    .queryString("query", ctx.getSearchString());

            if (StringUtils.isNotBlank(ctx.getCountry()))
                request = request.queryString("region", ctx.getCountry());

            return request;
        }

        @Override
        protected String getUrl() {
            return getGateConfiguration().getApiUrl();
        }

        @Override
        protected Path getPath() {
            return new Path("/place/textsearch/json");
        }
    }

    private static class GooglePlacesSearchNextPageCommand extends UnirestCommand<PlacesResponse> {
        private String nextPageToken;

        public GooglePlacesSearchNextPageCommand(String nextPageToken) {
            super("GooglePlaces.Search.NextPage", PlacesResponse.class);
            this.nextPageToken = nextPageToken;
        }

        @Override
        protected BaseRequest createRequest(String url, Path path) {
            return get(url, path)
                    .queryString("language", "en")
                    .queryString("key", getGateConfiguration().getGooglePlacesApiKey())
                    .queryString("pagetoken", nextPageToken);
        }

        @Override
        protected String getUrl() {
            return getGateConfiguration().getApiUrl();
        }

        @Override
        protected Path getPath() {
            return new Path("/place/textsearch/json");
        }
    }

    private static class GooglePlacesRefineCommand extends UnirestCommand<PlaceDetailsResponse> {
        private String placeId;

        public GooglePlacesRefineCommand(String placeId) {
            super("GooglePlaces.Refine", PlaceDetailsResponse.class);
            this.placeId = placeId;
        }

        @Override
        protected BaseRequest createRequest(String url, Path path) {
            return get(url, path)
                    .queryString("placeid", placeId)
                    .queryString("language", "en")
                    .queryString("key", getGateConfiguration().getGooglePlacesApiKey());
        }

        @Override
        protected String getUrl() {
            return getGateConfiguration().getApiUrl();
        }

        @Override
        protected Path getPath() {
            return new Path("/place/details/json");
        }
    }

    private static class GooglePlacesReverseGeocodeCommand extends UnirestCommand<PlacesResponse> {
        private ReverseGeocodingContext context;

        public GooglePlacesReverseGeocodeCommand(ReverseGeocodingContext context) {
            super("GooglePlaces.ReverseGeocode", PlacesResponse.class);
            this.context = context;
        }

        @Override
        protected BaseRequest createRequest(String url, Path path) {
            String sLat = String.valueOf(context.getSearchRegion().getLatitude());
            String sLon = String.valueOf(context.getSearchRegion().getLongitude());
            String sRad = String.valueOf(context.getSearchRegion().getRadius());

            return get(url, path)
                    .queryString("location", String.format("%s,%s", sLat, sLon))
                    .queryString("radius", sRad)
                    .queryString("language", "en")
                    .queryString("key", getGateConfiguration().getGooglePlacesApiKey());
        }

        @Override
        protected String getUrl() {
            return getGateConfiguration().getApiUrl();
        }

        @Override
        protected Path getPath() {
            return new Path("/place/nearbysearch/json");
        }
    }
}
