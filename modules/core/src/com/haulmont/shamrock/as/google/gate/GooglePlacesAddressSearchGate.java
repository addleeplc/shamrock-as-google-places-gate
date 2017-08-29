/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate;

import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.PropertyEvent;
import com.haulmont.shamrock.address.*;
import com.haulmont.shamrock.address.context.*;
import com.haulmont.shamrock.address.gis.GISUtils;
import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.address.utils.GeoHelper;
import com.haulmont.shamrock.address.utils.StringHelper;
import com.haulmont.shamrock.as.google.gate.dto.*;
import com.haulmont.shamrock.as.google.gate.utils.CityGeometry;
import com.haulmont.shamrock.as.google.gate.utils.Constants;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressUtils;
import com.haulmont.shamrock.geo.PostcodeHelper;
import groovy.util.ScriptException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by nikita on 05.07.17.
 */
public class GooglePlacesAddressSearchGate implements AddressSearchGate {
    public static final String API_KEY_SEARCH_PROPERTY = "api.key.search";
    public static final String API_KEY_REFINE_PROPERTY = "api.key.refine";
    public static final String API_KEY_REVERSE_GEOCODE_PROPERTY = "api.key.reverseGeocode";

    private static final Logger LOG = LoggerFactory.getLogger(GooglePlacesAddressSearchGate.class);

    private final Map<String, String> searchKeysCache = Collections.synchronizedMap(new HashMap<>(1));
    private final Map<String, String> refineKeysCache = Collections.synchronizedMap(new HashMap<>(1));
    private final Map<String, String> reverseGeocodeKeysCache = Collections.synchronizedMap(new HashMap<>(1));

    public GooglePlacesAddressSearchGate() {
        setupSearchKeysCache();
        setupRefineKeysCache();
        setupReverseGeocodeKeysCache();

        AppContext.getConfig().registerListener(propertyEvent -> {
            String key = propertyEvent.getKey();
            if (propertyEvent.getType() == PropertyEvent.EventType.UPDATE && !StringUtils.equals(propertyEvent.getOldValue(), propertyEvent.getValue())) {
                switch (key) {
                    case API_KEY_REFINE_PROPERTY:
                        LOG.info(
                                String.format(
                                        "%s property updated %s -> %s",
                                        key, propertyEvent.getOldValue(), propertyEvent.getValue()
                                )
                        );
                        updateRefineKeysCache(propertyEvent.getValue());
                        break;
                    case API_KEY_REVERSE_GEOCODE_PROPERTY:
                        LOG.info(
                                String.format(
                                        "%s property updated %s -> %s",
                                        key, propertyEvent.getOldValue(), propertyEvent.getValue()
                                )
                        );
                        updateReverseGeocodeKeysCache(propertyEvent.getValue());
                        break;
                    case API_KEY_SEARCH_PROPERTY:
                        LOG.info(
                                String.format(
                                        "%s property updated %s -> %s",
                                        key, propertyEvent.getOldValue(), propertyEvent.getValue()
                                )
                        );
                        updateSearchKeysCache(propertyEvent.getValue());
                        break;
                }
            }
        });
    }

    private void setupSearchKeysCache() {
        GateConfiguration conf = AppContext.getConfig().get(GateConfiguration.class);
        String key = conf.getSearchApiKey();

        searchKeysCache.putAll(getApiKeysByCountry(key));
    }

    private void updateSearchKeysCache(String key) {
        synchronized (searchKeysCache) {
            searchKeysCache.clear();
            searchKeysCache.putAll(getApiKeysByCountry(key));
        }
    }

    private void setupRefineKeysCache() {
        GateConfiguration conf = AppContext.getConfig().get(GateConfiguration.class);
        String key = conf.getRefineApiKey();

        refineKeysCache.putAll(getApiKeysByCountry(key));
    }

    private void updateRefineKeysCache(String key) {
        synchronized (refineKeysCache) {
            refineKeysCache.clear();
            refineKeysCache.putAll(getApiKeysByCountry(key));
        }
    }

    private void setupReverseGeocodeKeysCache() {
        GateConfiguration conf = AppContext.getConfig().get(GateConfiguration.class);
        String key = conf.getReverseGeocodeApiKey();

        reverseGeocodeKeysCache.putAll(getApiKeysByCountry(key));
    }

    private void updateReverseGeocodeKeysCache(String key) {
        synchronized (reverseGeocodeKeysCache) {
            reverseGeocodeKeysCache.clear();
            reverseGeocodeKeysCache.putAll(getApiKeysByCountry(key));
        }
    }

    private Map<String, String> getApiKeysByCountry(String apiKey) {
        if (apiKey.contains(";") || apiKey.contains(":")) {
            String[] tokens = apiKey.split(";");
            Map<String, String> res = new HashMap<>();
            for (String token : tokens) {
                String[] subTokens = token.split(":");
                if (subTokens.length == 2) {
                    res.put(subTokens[0], subTokens[1]);
                }
            }

            return res;
        } else {
            return Collections.singletonMap("DEFAULT", apiKey);
        }
    }

    @Override
    public String getId() {
        return "google-places";
    }

    public List<Address> searchBeneath(SearchBeneathContext context) {
        return Collections.emptyList();
    }

    @Override
    public List<Address> search(SearchContext context) {
        String searchString = context.getSearchString();
        if (StringUtils.isEmpty(searchString)) return null;

        List<Address> addresses;

        String postcode = PostcodeHelper.parsePostcode(context.getSearchString());
        if (postcode == null)
            postcode = context.getPostcode();
        boolean partialPostcode = PostcodeHelper.parsePostcode(postcode, false) == null;
        if (StringUtils.isNotBlank(postcode) && !partialPostcode &&
                context.getSearchString().equalsIgnoreCase(postcode)) {
            addresses = doSearch(context, 2);
        } else {

            if (StringUtils.isNotBlank(context.getCity()) && !StringUtils.containsIgnoreCase(searchString, context.getCity())) {
                SearchContext temp = clone(context);
                temp.setCity(context.getCity());
                temp.setSearchString(searchString + ", " + context.getCity());

                addresses = doSearch(temp, 2);
            } else {
                addresses = doSearch(context, 2);

                if (StringUtils.isNotBlank(context.getPreferredCity()) && !StringUtils.containsIgnoreCase(searchString, context.getPreferredCity())) {
                    boolean haveGoodMatches = false;
                    for (Address o : addresses) {
                        if (StringUtils.equalsIgnoreCase(o.getAddressData().getAddressComponents().getCity(), context.getPreferredCity())) {
                            haveGoodMatches = true;
                            break;
                        }
                    }

                    //First step: add preferred country to search string
                    if (!haveGoodMatches) {
                        SearchContext temp = clone(context);
                        temp.setCountry(context.getPreferredCountry());
                        //Google Address Search API works well with full country name instead of ISO country code
                        temp.setSearchString(searchString + ", " + Constants.Country.isoToCountry.get(context.getPreferredCountry()));

                        List<Address> pcAddresses = doSearch(temp, 2);
                        addresses.addAll(pcAddresses);
                    }

                    //Second step: add preferred country and city
                    if (!haveGoodMatches) {
                        SearchContext temp = clone(context);
                        temp.setCity(context.getPreferredCity());
                        temp.setCountry(context.getPreferredCountry());

                        //Google Address Search API works well with full country name instead of ISO country code
                        temp.setSearchString(searchString + ", " + context.getPreferredCity() + ", " + Constants.Country.isoToCountry.get(context.getPreferredCountry()));

                        List<Address> pcAddresses = doSearch(temp, 2);
                        addresses.addAll(pcAddresses);
                    }
                }
            }
        }

        return filter(addresses);
    }

    @Override
    public Address refine(RefineContext context) throws RuntimeException {
        return __refine(context);
    }

    @Override
    public Address geocode(GeocodeContext context) {
        throw new UnsupportedOperationException("Unsupported for " + getId() + " gate");
    }

    @Override
    public List<Address> reverseGeocode(ReverseGeocodingContext context) {
        String url;
        try {
            url = String.format(
                    "%s/nearbysearch/json?location=%s,%s&radius=%s&language=en&key=%s",
                    getGateConfiguration().getApiUrl(),
                    URLEncoder.encode(String.valueOf(context.getSearchRegion().getLatitude()), "UTF-8"),
                    URLEncoder.encode(String.valueOf(context.getSearchRegion().getLongitude()), "UTF-8"),
                    URLEncoder.encode(String.valueOf(context.getSearchRegion().getRadius()), "UTF8"),
                    URLEncoder.encode(getReverseGeocodingGoogleApiKey(context), "UTF8")
            );
        } catch (UnsupportedEncodingException | ScriptException e) {
            throw new RuntimeException(e);
        }

        try {
            //noinspection unchecked
            List<Address> addresses = doSearch(url, PlacesResponse.class, new Converter<PlacesResponse>() {
                public List<Address> convert(PlacesResponse response) {
                    List<Address> res = new ArrayList<>();

                    List<PlacesResult> results = response.getResults();
                    for (PlacesResult o : results) {
                        try {
                            if (o.getTypes().size() == 1 && o.getTypes().contains("route")) continue;

                            Address tmp = new Address();

                            String name = StringHelper.convertToAscii(o.getName().trim());
                            tmp.setId(getId() + "|" + o.getPlace_id());
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

                            Address a = __refine(refineContext);
                            if (isAddressCoordinatesBlank(a)) {
                                Location l = a.getAddressData().getLocation();
                                double distance = GeoHelper.getGeoDistance(l.getLon(), l.getLat(), context.getSearchRegion().getLongitude(), context.getSearchRegion().getLatitude());
                                if (distance <= context.getSearchRegion().getRadius()) {
                                    a.setDistance(distance);
                                    res.add(a);
                                }
                            }
                        } catch (Throwable e) {
                            LOG.warn("Fail to parse address: " + (o.getName() + ", " + o.getVicinity()), e);
                        }
                    }

                    LOG.info(String.format(
                            "Search addresses near (%s, %s), radius %s, found: %s",
                            context.getSearchRegion().getLatitude(), context.getSearchRegion().getLongitude(),
                            context.getSearchRegion().getRadius(), res.size())
                    );

                    return res;
                }
            });

            return filter(addresses);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private SearchContext clone(SearchContext context) {
        SearchContext clone = new SearchContext();
        clone.setSearchString(context.getSearchString());
        clone.setCountry(context.getCountry());
        clone.setMaxResults(context.getMaxResults());
        clone.setCity(context.getCity());
        clone.setStartIndex(context.getStartIndex());
        clone.setFlatten(context.isFlatten());
        clone.setPreferredCity(context.getPreferredCity());
        clone.setProviders(context.getProviders());
        clone.setSearchBusinessNames(context.isSearchBusinessNames());
        clone.setSearchFlats(context.isSearchFlats());

        return clone;
    }

    private String getSearchGoogleApiKey(SearchContext searchContext) throws ScriptException {
        String country = searchContext.getCountry();
        if (StringUtils.isNotBlank(country) && searchKeysCache.containsKey(country)) {
            return searchKeysCache.get(country);
        } else {
            //we assume that DEFAULT key always exists
            return searchKeysCache.get("DEFAULT");
        }
    }

    private String getReverseGeocodingGoogleApiKey(ReverseGeocodingContext rgContext) throws ScriptException {
        GeoRegion searchRegion = rgContext.getSearchRegion();
        String country = getCountry(searchRegion.getLatitude(), searchRegion.getLongitude());
        if (StringUtils.isNotBlank(country) && reverseGeocodeKeysCache.containsKey(country)) {
            return reverseGeocodeKeysCache.get(country);
        } else {
            //we assume that DEFAULT key always exists
            return reverseGeocodeKeysCache.get("DEFAULT");
        }
    }

    private String getCountry(double latitude, double longitude) {
        for (CityGeometry g : GeometryConstants.CITIES) {
            if (GISUtils.isInsideGeometry(g, latitude, longitude)) {
                return g.getCountry();
            }
        }

        return null;
    }

    private String getRefineGoogleApiKey(Address address) throws ScriptException {
        String country = address.getAddressData().getAddressComponents().getCountry();
        if (StringUtils.isNotBlank(country) && refineKeysCache.containsKey(country)) {
            return refineKeysCache.get(country);
        } else {
            //we assume that GB key always exists
            return refineKeysCache.get("DEFAULT");
        }
    }

    private Address __refine(RefineContext context) {
        if (AddressHelper.isAddressEx(context.getAddress())) {
            return AddressHelper.convert(context.getAddress(), context.getRefineType());
        } else {
            Address a = context.getAddress();
            try {
                String id = AddressHelper.getAddressId(a);
                if (id == null)
                    return null;

                String url = String.format("%s/details/json?placeid=%s&language=en&key=%s",
                        getGateConfiguration().getApiUrl(),
                        URLEncoder.encode(id, "UTF8"),
                        URLEncoder.encode(getRefineGoogleApiKey(context.getAddress()), "UTF8")
                );

                List<Address> searchRes = doSearch(url, PlaceDetailsResponse.class, new Converter<PlaceDetailsResponse>() {
                    public List<Address> convert(PlaceDetailsResponse response) {
                        PlaceDetailsResult details = response.getResult();
                        Map<String, AddressComponent> components = GoogleAddressUtils.convert(details.getAddress_components());

                        try {
                            Address res = parseAddress(details.getFormatted_address(), getRequestedCountry(context), details.getGeometry(), components, details.getTypes());

                            if (res != null) {
                                res.setId(String.format("%s|%s", getId(), details.getId()));
                            } else {
                                return null;
                            }

                            GoogleAddressUtils.assignPlaceDetails(res, details);

                            return Collections.singletonList(res);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                if (CollectionUtils.isNotEmpty(searchRes)) {
                    Address res = searchRes.get(0);
                    LOG.info(String.format("Refine address '%s/%s' (%s, %s), result: %s",
                            a.getId(), a.getAddressData().getFormattedAddress(),
                            context.getRefineType().name(), getRequestedCountry(context),
                            res != null ? res.getAddressData().getFormattedAddress() : null)
                    );
                    return res;
                }
            } catch (Throwable e) {
                LOG.warn("Failed to refine address: " + a.getId(), e);
            }

            return null;
        }
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

    private List<Address> doSearch(final SearchContext context, int maxLookupPages) {
        List<PlacesResponse> responses = new ArrayList<>();

        PlacesResponse previous = null;
        for (int i = 0; i < maxLookupPages; ++i) {
            try {
                String url;
                if (i == 0) {
                    url = String.format("%s/textsearch/json?query=%s&language=en&key=%s",
                            getGateConfiguration().getApiUrl(),
                            URLEncoder.encode(context.getSearchString(), "UTF8"),
                            URLEncoder.encode(getSearchGoogleApiKey(context), "UTF8")
                    );
                } else if (previous != null && StringUtils.isNotBlank(previous.getNextPageToken())) {
                    url = String.format("%s/textsearch/json?pagetoken=%s&language=en&key=%s",
                            getGateConfiguration().getApiUrl(),
                            URLEncoder.encode(previous.getNextPageToken(), "UTF8"),
                            URLEncoder.encode(getSearchGoogleApiKey(context), "UTF8")
                    );
                } else {
                    break;
                }

                PlacesResponse response = doRequest(url, PlacesResponse.class);

                GoogleApiStatus status = GoogleApiStatus.fromString(response.getStatus());
                if (status != GoogleApiStatus.OK)
                    break;

                responses.add(response);
                if (responses.size() == maxLookupPages && StringUtils.isNotBlank(response.getNextPageToken())) {
                    return Collections.emptyList();
                }

                previous = response;
            } catch (Throwable e) {
                LOG.warn("Failed to search address", e);
            }
        }

        if (CollectionUtils.isEmpty(responses))
            return Collections.emptyList();

        return responses.parallelStream()
                .map(p -> new SearchPlacesConverter(context).convert(p))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<Address> doSearch(String url, Class responseClass, Converter converter) throws Throwable {
        Object response = doRequest(url, responseClass);

        Method method = responseClass.getMethod("getStatus");
        GoogleApiStatus status = GoogleApiStatus.fromString((String) method.invoke(response));

        LOG.debug(String.format("Load addresses from url: '%s', result: '%s'", url, status));

        if (status == GoogleApiStatus.REQUEST_DENIED
                || status == GoogleApiStatus.INVALID_REQUEST
                || status == GoogleApiStatus.OVER_QUERY_LIMIT
                || status == GoogleApiStatus.UNKNOWN_ERROR) {
            return Collections.emptyList();
        }

        if (status == GoogleApiStatus.OK) {
            return converter.convert(response);
        } else if (status == GoogleApiStatus.ZERO_RESULTS) {
            return Collections.emptyList();
        } else {
            return Collections.emptyList();
        }
    }

    private static Address parseAddress(String formattedAddress, String reqCountry,
                                        Geometry geometry,
                                        Map<String, AddressComponent> components,
                                        List<String> types) {

        try {
            return GoogleAddressUtils.parseAddress(formattedAddress, reqCountry, geometry, components, types);
        } catch (GoogleAddressUtils.AddressParseException e) {
            LOG.debug(String.format("Failed to parse address '%s': %s", formattedAddress, e.getMessage()));
        }

        return null;
    }

    private GateConfiguration getGateConfiguration() {
        return AppContext.getConfig().get(GateConfiguration.class);
    }

    private <T> T doRequest(String request, Class<T> responseClass) throws IOException {
        URL url = new URL(request);

        URLConnection connection = url.openConnection();
        connection.setReadTimeout(getGateConfiguration().getTimeout());

        return GoogleAddressUtils.parseResponse(responseClass, connection.getInputStream());
    }

    //

    private static final Pattern GB_POSTCODE_PATTERN = Pattern.compile(
            "^[A-Z]{1,2}[0-9]{1}([A-Z]|[0-9])?\\s{1}[0-9]{1}[A-Z]{2}$",
            Pattern.CASE_INSENSITIVE
    );

    private List<Address> filter(List<Address> addresses) {
        return addresses.parallelStream()
                .filter(address -> address != null)
                .filter(address -> address.getAddressData() != null)
                .filter(address -> address.getAddressData().getAddressComponents() != null)
                .filter(address -> StringUtils.isNotBlank(address.getAddressData().getAddressComponents().getAddress()))
                .filter(address -> !containsJunkWords(address))
                .filter(address -> address.getAddressData().getAddressComponents().getPostcode() != null)
                .filter(address -> !"GB".equals(address.getAddressData().getAddressComponents().getCountry()) || GB_POSTCODE_PATTERN.matcher(address.getAddressData().getAddressComponents().getPostcode()).find())
                .collect(Collectors.toList());
    }

    private static final List<String> JUNK_WORDS = Collections.synchronizedList(Arrays.asList(
            "null"
    ));

    private boolean containsJunkWords(Address a) {
        String s = a.getAddressData().getAddressComponents().getAddress();

        for (String word : JUNK_WORDS) {
            if (StringUtils.containsIgnoreCase(s, word))
                return true;
        }

        return false;
    }

    protected interface Converter<T> {
        List<Address> convert(T response);
    }

    private class SearchPlacesConverter implements Converter<PlacesResponse> {

        private SearchContext context;

        public SearchPlacesConverter(SearchContext context) {
            this.context = context;
        }

        public List<Address> convert(PlacesResponse response) {
            List<Address> res1 = new ArrayList<>();

            List<PlacesResult> results = response.getResults();
            for (PlacesResult o : results) {
                try {
                    Address a = new Address();

                    AddressData ad = new AddressData();
                    AddressComponents ac = new AddressComponents();

                    a.setId(String.format("%s|%s", getId(), o.getPlace_id()));
                    ad.setFormattedAddress(o.getName() + ", " + o.getVicinity());

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
                        res1.add(a);
                    }
                } catch (Throwable e) {
                    LOG.warn("Failed to parse address", e);
                }
            }

            return res1;
        }
    }
}
