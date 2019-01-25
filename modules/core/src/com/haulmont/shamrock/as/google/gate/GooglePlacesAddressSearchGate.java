package com.haulmont.shamrock.as.google.gate;

import com.google.common.collect.Lists;
import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.ServiceException;
import com.haulmont.monaco.config.Properties;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.shamrock.address.*;
import com.haulmont.shamrock.address.context.*;
import com.haulmont.shamrock.address.gis.GISUtils;
import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.address.utils.GeoHelper;
import com.haulmont.shamrock.address.utils.StringHelper;
import com.haulmont.shamrock.as.google.gate.constants.GeometryConstants;
import com.haulmont.shamrock.as.google.gate.converters.PlaceDetailsConverterService;
import com.haulmont.shamrock.as.google.gate.dto.Geometry;
import com.haulmont.shamrock.as.google.gate.dto.Place;
import com.haulmont.shamrock.as.google.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.parsers.PlaceParsingService;
import com.haulmont.shamrock.as.google.gate.services.GooglePlacesService;
import com.haulmont.shamrock.as.google.gate.utils.CityGeometry;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressSearchUtils;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressUtils;
import com.haulmont.shamrock.geo.PostcodeHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class GooglePlacesAddressSearchGate implements AddressSearchGate {

    @Inject
    private Logger logger;

    @Inject
    private GooglePlacesService googlePlacesService;

    @Inject
    private PlaceParsingService placeParsingService;

    @Inject
    private PlaceDetailsConverterService placeDetailsConverterService;

    @Inject
    private ServiceConfiguration configuration;

    //

    private Properties properties;

    //

    public GooglePlacesAddressSearchGate() {
        properties = AppContext.getConfig().getProperties();
    }

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

        List<Address> addresses = new ArrayList<>();

        String postcode = PostcodeHelper.parsePostcode(context.getSearchString());
        if (postcode == null) postcode = context.getPostcode();

        boolean partialPostcode = PostcodeHelper.parsePostcode(postcode, false) == null;
        if (StringUtils.isNotBlank(postcode) && !partialPostcode && context.getSearchString().equalsIgnoreCase(postcode)) {
            addresses.addAll(doSearch(context));
        } else {
            if (StringUtils.isNotBlank(context.getCity())) {
                SearchContext temp = GoogleAddressSearchUtils.clone(context);
                temp.setCity(context.getCity());
                temp.setSearchString(searchString + ", " + context.getCity());

                addresses.addAll(doSearch(temp));
            } else {
                //First step
                addresses.addAll(doSearch(context));

                if (CollectionUtils.isEmpty(addresses)) {
                    SearchContext temp = GoogleAddressSearchUtils.clone(context);
                    temp.setCity(context.getPreferredCity());
                    temp.setCountry(context.getPreferredCountry());
                    temp.setSearchString(searchString + ", " + context.getPreferredCity());

                    addresses.addAll(doSearch(temp));
                }
            }
        }

        final List<Address> res = GoogleAddressSearchUtils.filter(addresses);

        logger.debug("Search address by text (text: '{}', resSize: {}) ({} ms)'", context.getSearchString(), res.size(), System.currentTimeMillis() - ts);

        return res;
    }

    private List<Address> doSearch(final SearchContext context) {
        List<Place> places = new ArrayList<>();

        try {
            places = googlePlacesService.getPlaces(context);
        } catch (Throwable e) {
            logger.warn("Failed to search address", e);
        }

        if (CollectionUtils.isEmpty(places)) return Collections.emptyList();

        List<Address> res = new ArrayList<>();
        for (Place place : places) {
            if (!isValidPlaceResult(place)) continue;

            Address address = convert(context, place);
            if (address != null) res.add(address);
        }

        return res;
    }

    private boolean isValidPlaceResult(Place pr) {
        List<String> types = pr.getTypes();
        if (CollectionUtils.isEmpty(types)) return false;

        if (types.size() == 1) {
            return !org.apache.commons.collections4.CollectionUtils.containsAny(
                    types,
                    Arrays.asList(GElement.postal_code.name(), GElement.postal_town.name(), GElement.postal_code_prefix.name(), GElement.postal_code_suffix.name())
            );
        }

        return true;
    }

    private Address convert(SearchContext context, Place place) {
        try {
            Address res = convert(place);

            AddressData data = res.getAddressData();
            AddressComponents components = data.getAddressComponents();

            if (context.getCountry() != null) {
                components.setCountry(context.getCountry());
            }

            if (BooleanUtils.isTrue(configuration.getEnableFormattedAddressParsing())) {
                Address address = placeParsingService.parse(place, getId());
                if (address == null) {
                    return refine(res);
                } else {
                    return address;
                }
            } else {
                return refine(res);
            }
        } catch (Throwable e) {
            logger.warn("Failed to parse address", e);
            return null;
        }
    }

    private Address refine(Address res) {
        RefineContext context = new RefineContext();
        context.setAddress(res);
        context.setRefineType(RefineType.DEFAULT);

        return refine(context);
    }


    private Address convert(Place place) {
        Address res = new Address();

        AddressData data = new AddressData();
        AddressComponents components = new AddressComponents();

        res.setId(String.format("%s|%s", getId(), place.getPlaceId()));
        data.setFormattedAddress(GoogleAddressUtils.getFormattedAddress(place));

        data.setAddressComponents(components);
        res.setAddressData(data);

        return res;
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
                if (id == null) return null;

                PlaceDetails placeDetails = googlePlacesService.getPlaceDetails(id);

                if (placeDetails == null) {
                    return null;
                } else {
                    Address address = convertRefineResult(placeDetails);
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
            } catch (ServiceException e) {
                throw e;
            } catch (Throwable t) {
                throw new ServiceException(ErrorCode.SERVER_ERROR, "Unknown error", t);
            }
        }
    }

    private Address convertRefineResult(PlaceDetails details) {
        return placeDetailsConverterService.convert(details, getId());
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
            List<Place> places = googlePlacesService.getPlaces(context);

            if (CollectionUtils.isEmpty(places)) {
                res = Collections.emptyList();
            } else {
                res = new ArrayList<>();
                for (List<Place> list : Lists.partition(places, 5)) {
                    List<Address> addresses = convertReverseGeocodeResults(context, list);
                    if (CollectionUtils.isNotEmpty(addresses)) res.addAll(addresses);
                }
            }

            Address o = res.isEmpty() ? null : res.get(0);

            logger.debug("Reverse geocode address by location (loc: {},{}, res: '{}', resSize: {}) ({} ms)", context.getSearchRegion().getLatitude(), context.getSearchRegion().getLongitude(), o != null ? o.getAddressData().getFormattedAddress() : "N/A", res.size(), System.currentTimeMillis() - ts);

            return res;
        } catch (ServiceException e) {
            throw e;
        } catch (Throwable t) {
            throw new ServiceException(ErrorCode.SERVER_ERROR, "Unknown error", t);
        }
    }

    private List<Address> convertReverseGeocodeResults(ReverseGeocodingContext context, List<Place> results) {
        List<Address> res = new ArrayList<>();
        for (Place o : results) {
            try {
                if (o.getTypes().size() == 1 && o.getTypes().contains("route")) continue;

                Address tmp = new Address();
                AddressData data = new AddressData();
                tmp.setAddressData(data);

                tmp.setId(getId() + "|" + o.getPlaceId());
                data.setFormattedAddress(GoogleAddressUtils.getFormattedAddress(o));

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
                logger.warn("Fail to parse address: " + (GoogleAddressUtils.getFormattedAddress(o)), e);
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
}
