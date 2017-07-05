/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate;

import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.PropertyEvent;
import com.haulmont.shamrock.address.Address;
import com.haulmont.shamrock.address.AddressSearchGate;
import com.haulmont.shamrock.address.GeocodeContext;
import com.haulmont.shamrock.address.Location;
import com.haulmont.shamrock.address.context.RefineContext;
import com.haulmont.shamrock.address.context.ReverseGeocodingContext;
import com.haulmont.shamrock.address.context.SearchBeneathContext;
import com.haulmont.shamrock.address.context.SearchContext;
import com.haulmont.shamrock.address.gis.GISUtils;
import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.address.utils.GeoHelper;
import com.haulmont.shamrock.as.google.gate.dto.*;
import com.haulmont.shamrock.as.google.gate.utils.CityGeometry;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressUtils;
import groovy.util.ScriptException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Nikita Bozhko on 07.01.17.
 * Project Shamrock
 */
public class GoogleGeocodeAddressSearchGate implements AddressSearchGate {
    public static final String API_KEY_GEOCODE_PROPERTY = "api.key.geocode";
    public static final String API_KEY_SEARCH_PROPERTY = "api.key.search";

    private static final Logger LOG = LoggerFactory.getLogger(GoogleGeocodeAddressSearchGate.class);

    public static final Pattern GB_POSTCODE_PATTERN = Pattern.compile(
            "^[A-Z]{1,2}[0-9]{1}([A-Z]|[0-9])?\\s{1}[0-9]{1}[A-Z]{2}$",
            Pattern.CASE_INSENSITIVE
    );

    private final Map<String, String> searchKeysCache = Collections.synchronizedMap(new HashMap<>(1));
    private final Map<String, String> geocodeKeysCache = Collections.synchronizedMap(new HashMap<>(1));

    public GoogleGeocodeAddressSearchGate() {
        setupSearchKeysCache();
        setupGeocodeKeysCache();

        AppContext.getConfig().registerListener(propertyEvent -> {
            String key = propertyEvent.getKey();
            if (propertyEvent.getType() == PropertyEvent.EventType.UPDATE && !StringUtils.equals(propertyEvent.getOldValue(), propertyEvent.getValue())) {
                switch (key) {
                    case API_KEY_GEOCODE_PROPERTY:
                        LOG.info(
                                String.format(
                                        "%s property updated %s -> %s",
                                        key, propertyEvent.getOldValue(), propertyEvent.getValue()
                                )
                        );
                        updateGeocodeKeysCache(propertyEvent.getValue());
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

    private void setupGeocodeKeysCache() {
        GateConfiguration conf = AppContext.getConfig().get(GateConfiguration.class);
        String key = conf.getGeocodeApiKey();

        geocodeKeysCache.putAll(getApiKeysByCountry(key));
    }

    private void updateGeocodeKeysCache(String key) {
        synchronized (geocodeKeysCache) {
            geocodeKeysCache.clear();
            geocodeKeysCache.putAll(getApiKeysByCountry(key));
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
        return "google-geocode";
    }

    public List<Address> searchBeneath(SearchBeneathContext context) {
        return Collections.emptyList();
    }

    @Override
    public List<Address> search(SearchContext context) {
        try {
            String url = String.format(
                    getGateConfiguration().getApiUrl() +
                    "?address=%s&language=en&key=%s",
                    URLEncoder.encode(context.getSearchString(), "UTF8"),
                    URLEncoder.encode(getSearchGoogleApiKey(context), "UTF8"));

            if (StringUtils.isNotBlank(context.getCountry()) ||
                    StringUtils.isNotBlank(context.getPostcode()) ||
                    StringUtils.isNotBlank(context.getCity()))
            {
                boolean f = true;

                StringBuilder buffer = new StringBuilder("components=");
                if (StringUtils.isNotBlank(context.getCountry())) {
                    buffer.append("country:").append(context.getCountry());
                    f = false;
                }

                if (StringUtils.isNotBlank(context.getCity())) {
                    if (!f) buffer.append("|");
                    buffer.append("locality:").append(URLEncoder.encode(context.getCity(), "UTF-8"));
                    f = false;
                }

                url += "&" + buffer.toString();
            }

            Address address = doSearch(url, GeocodingResponse.class, new Converter<GeocodingResponse>() {
                public Address convert(GeocodingResponse response) {
                    List<GeocodingResult> results = response.getResults();
                    for (GeocodingResult o : results) {
                        Map<String, AddressComponent> components = GoogleAddressUtils.convert(o.getAddress_components());

                        try {
                            Address a = parseAddress(o.getFormatted_address(), context.getCountry(), o.getGeometry(), components, o.getTypes());

                            if (a != null) {
                                a.setId(String.format("%s|%s", getId(), null));
                                return a;
                            }
                        } catch (Throwable e) {
                            LOG.warn("Failed to parse address", e);
                        }
                    }

                    return null;
                }
            });

            return address == null ? Collections.<Address>emptyList() : filter(Collections.singletonList(address));
        } catch (Throwable e) {
            LOG.warn("Failed to search address", e);
            return Collections.emptyList();
        }
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

    private String getSearchGoogleApiKey(SearchContext searchContext) throws ScriptException {
        String country = searchContext.getCountry();
        if (StringUtils.isNotBlank(country) && searchKeysCache.containsKey(country)) {
            return searchKeysCache.get(country);
        } else {
            //we assume that DEFAULT key always exists
            return searchKeysCache.get("DEFAULT");
        }
    }

    private String getGeocodeGoogleApiKey(GeocodeContext geocodeContext) throws ScriptException {
        String country = null;
        if (geocodeContext.getLocation() != null) {
            Location location = geocodeContext.getLocation();
            if (location.getLat() != null && location.getLon() != null) {
                country = getCountry(location.getLat(), location.getLon());
            }
        } else {
            country = geocodeContext.getCountry();
        }

        if (StringUtils.isNotBlank(country) && geocodeKeysCache.containsKey(country)) {
            return geocodeKeysCache.get(country);
        } else {
            return geocodeKeysCache.get("DEFAULT");
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

    private Address doSearch(String url, Class responseClass, Converter converter) throws Throwable {
        Object response = doRequest(url, responseClass);

        Method method = responseClass.getMethod("getStatus");
        GoogleApiStatus status = GoogleApiStatus.fromString((String) method.invoke(response));

        LOG.debug(String.format("Load addresses from url: '%s', result: '%s'", url, status));

        if (status == GoogleApiStatus.REQUEST_DENIED
                || status == GoogleApiStatus.INVALID_REQUEST
                || status == GoogleApiStatus.OVER_QUERY_LIMIT
                || status == GoogleApiStatus.UNKNOWN_ERROR) {
            return null;
        }

        if (status == GoogleApiStatus.OK) {
            return converter.convert(response);
        } else if (status == GoogleApiStatus.ZERO_RESULTS) {
            return null;
        } else {
            return null;
        }
    }

    private Address geocodeByAddress(final GeocodeContext context) {
        try {
            String url = String.format(
                    getGateConfiguration().getApiUrl() +
                    "?address=%s&language=en&key=%s",
                    URLEncoder.encode(context.getAddress(), "UTF8"),
                    URLEncoder.encode(getGeocodeGoogleApiKey(context), "UTF8"));

            if (StringUtils.isNotBlank(context.getCountry()) ||
                    StringUtils.isNotBlank(context.getPostcode()) ||
                    StringUtils.isNotBlank(context.getCity()))
            {
                boolean f = true;

                StringBuilder buffer = new StringBuilder("components=");
                if (StringUtils.isNotBlank(context.getCountry())) {
                    buffer.append("country:").append(context.getCountry());
                    f = false;
                }

                if (StringUtils.isNotBlank(context.getCity())) {
                    if (!f) buffer.append("|");
                    buffer.append("locality:").append(URLEncoder.encode(context.getCity(), "UTF-8"));
                    f = false;
                }

                url += "&" + buffer.toString();
            }


            return doSearch(url, GeocodingResponse.class, new Converter<GeocodingResponse>() {
                public Address convert(GeocodingResponse response) {
                    List<GeocodingResult> results = response.getResults();
                    for (GeocodingResult o : results) {
                        Map<String, AddressComponent> components = GoogleAddressUtils.convert(o.getAddress_components());

                        try {
                            Address a = parseAddress(o.getFormatted_address(), context.getCountry(), o.getGeometry(), components, o.getTypes());

                            if (a != null) {
                                a.setId(String.format("%s|%s", getId(), null));
                                return a;
                            }
                        } catch (Throwable e) {
                            LOG.warn("Failed to parse address", e);
                        }
                    }

                    return null;
                }
            });
        } catch (Throwable e) {
            LOG.warn("Failed to search address", e);
        }

        return null;
    }

    private Address geocodeByLocation(final GeocodeContext context) {
        try {
            String url = String.format(
                    getGateConfiguration().getApiUrl() +
                    "?latlng=%.6f,%.6f&location_type=ROOFTOP&result_type=street_address&language=en&key=%s",
                    context.getLocation().getLat(), context.getLocation().getLon(),
                    URLEncoder.encode(getGeocodeGoogleApiKey(context), "UTF8")
            );

            return doSearch(url, GeocodingResponse.class, new Converter<GeocodingResponse>() {
                public Address convert(GeocodingResponse response) {
                    List<GeocodingResult> results = response.getResults();
                    for (GeocodingResult o : results) {
                        Map<String, AddressComponent> components = GoogleAddressUtils.convert(o.getAddress_components());

                        try {
                            Address a = parseAddress(o.getFormatted_address(), context.getCountry(), o.getGeometry(), components, o.getTypes());

                            if (a != null) {
                                a.setId(String.format("%s|%s", getId(), null));

                                return a;
                            }
                        } catch (Throwable e) {
                            LOG.warn("Failed to parse address", e);
                        }
                    }

                    return null;
                }
            });
        } catch (Throwable e) {
            LOG.warn("Failed to geocode address", e);
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

    protected List<Address> filter(List<Address> addresses) {
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
        Address convert(T response);
    }
}
