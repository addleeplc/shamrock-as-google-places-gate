/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.rs.v1;

import com.haulmont.monaco.ServiceException;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.monaco.response.Response;
import com.haulmont.shamrock.as.context.AutocompleteContext;
import com.haulmont.shamrock.as.contexts.*;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.dto.LatLon;
import com.haulmont.shamrock.as.dto.LocationWithAccuracy;
import com.haulmont.shamrock.as.google.places.gate.GooglePlacesAddressSearchGate;
import com.haulmont.shamrock.as.google.places.gate.dto.Geometry;
import com.haulmont.shamrock.as.google.places.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.places.gate.rs.v1.dto.*;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.LatLng;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Nikita Bozhko on 01.01.17.
 * Project Shamrock
 */

@Path("/v1")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class GoogleGateResource {

    public static final Pattern LOCATION_PATTERN = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+),([-+]?[0-9]*\\.?[0-9]+)");
    private static final Logger logger = LoggerFactory.getLogger(GoogleGateResource.class);
    @Inject
    private GooglePlacesAddressSearchGate gate;

    @GET
    @Path("/search")
    public SearchResponse search(
            @QueryParam("search_string") String searchString,
            @QueryParam("preferred_city") String preferredCity,
            @QueryParam("preferred_country") String preferredCountry,
            @QueryParam("city") String city,
            @QueryParam("country") String country,
            @QueryParam("postcode") String postcode,
            @QueryParam("search_flats") boolean searchFlats,
            @QueryParam("search_business_names") boolean searchBusinessNames,
            @QueryParam("flatten") boolean flatten,
            @QueryParam("start_index") Integer startIndex,
            @QueryParam("max_results") Integer maxResults
    ) {
        if (StringUtils.isNotBlank(searchString)) {
            SearchContext ctx = new SearchContext();
            ctx.setSearchString(searchString);

            if (StringUtils.isNotBlank(preferredCity))
                ctx.setPreferredCity(preferredCity);

            if (StringUtils.isNotBlank(preferredCountry))
                ctx.setPreferredCountry(preferredCountry);

            if (StringUtils.isNotBlank(city))
                ctx.setCity(city);

            if (StringUtils.isNotBlank(country))
                ctx.setCountry(country);

            if (StringUtils.isNotBlank(postcode))
                ctx.setPostcode(postcode);

            ctx.setSearchFlats(searchFlats);
            ctx.setSearchBusinessNames(searchBusinessNames);
            ctx.setFlatten(flatten);
            ctx.setStartIndex(startIndex);
            ctx.setMaxResults(maxResults);

            return new SearchResponse(ErrorCode.OK, gate.search(ctx));
        } else {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Parameter 'search_string' must be non-null");
        }
    }

    @GET
    @Path("/autocomplete")
    public SearchResponse autocomplete(
            @QueryParam("search_string") String searchString,
            @QueryParam("preferred_city") String preferredCity,
            @QueryParam("preferred_country") String preferredCountry,
            @QueryParam("city") String city,
            @QueryParam("country") String country,
            @QueryParam("origin") String origin,
            @QueryParam("location") String location,
            @QueryParam("radius") Double radius
    ) {
        if (StringUtils.isNotBlank(searchString)) {
            AutocompleteContext ctx = new AutocompleteContext();
            ctx.setSearchString(searchString);

            if (StringUtils.isNotBlank(preferredCity))
                ctx.setPreferredCity(preferredCity);

            if (StringUtils.isNotBlank(preferredCountry))
                ctx.setPreferredCountry(preferredCountry);

            if (StringUtils.isNotBlank(city))
                ctx.setCity(city);

            if (StringUtils.isNotBlank(country))
                ctx.setCountry(country);

            if (StringUtils.isNotBlank(origin)) {
                try {
                    LatLon o = parseLatLon(origin);

                    LocationWithAccuracy l = new LocationWithAccuracy();

                    l.setLat(o.getLat());
                    l.setLon(o.getLon());

                    ctx.setOrigin(l);
                } catch (IllegalArgumentException e) {
                    throw new ServiceException(ErrorCode.BAD_REQUEST, "Can't parse 'origin'", e);
                }
            }

            if (StringUtils.isNotBlank(location)) {
                try {
                    LatLon o = parseLatLon(location);

                    com.haulmont.shamrock.as.dto.CircularRegion l = new com.haulmont.shamrock.as.dto.CircularRegion();

                    l.setLat(o.getLat());
                    l.setLon(o.getLon());
                    l.setRadius(radius == null ? 1000.0 : radius);

                    ctx.setSearchRegion(l);
                } catch (IllegalArgumentException e) {
                    throw new ServiceException(ErrorCode.BAD_REQUEST, "Can't parse 'location'", e);
                }
            }

            return new SearchResponse(ErrorCode.OK, gate.autocomplete(ctx));
        } else {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Parameter 'search_string' must be non-null");
        }
    }

    private LatLon parseLatLon(String s) {
        LatLon o;

        Matcher matcher = LOCATION_PATTERN.matcher(s);
        if (matcher.matches()) {
            o = new LatLon();

            String slat = matcher.group(1);
            String slon = matcher.group(2);

            o.setLat(Double.valueOf(slat));
            o.setLon(Double.valueOf(slon));
        } else {
            throw new IllegalArgumentException("Can't parse:" + s);
        }

        return o;
    }

    @POST
    @Path("/refine")
    public RefineResponse refine(
            @Context HttpServletRequest request,
            Address address
    ) {
        String refineType = request.getParameter("refine_type");
        RefineType type = StringUtils.equals("ONLY_BUILDING", refineType) ?
                RefineType.ONLY_BUILDING :
                StringUtils.equals("ONLY_STREET", refineType) ?
                        RefineType.ONLY_STREET :
                        RefineType.DEFAULT;

        RefineContext ctx = new RefineContext();
        ctx.setRefineType(type);
        ctx.setAddress(address);

        String id = address.getId();
        if (StringUtils.containsIgnoreCase(id, "google-places")) {
            return new RefineResponse(ErrorCode.OK, gate.refine(ctx));
        } else {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Address Id is invalid");
        }
    }

    @GET
    @Path("/geocode")
    public Response geocode() {
        logger.debug("Unsupported for google-places-gate");
        return new Response(ErrorCode.OK);
    }

    @GET
    @Path("/reverse-geocode")
    public ReverseGeocodingResponse reverseGeocode(
            @QueryParam("latitude") Double latitude,
            @QueryParam("longitude") Double longitude,
            @QueryParam("radius") Double radius
    ) {
        if (latitude != null && longitude != null) {
            ReverseGeocodingContext ctx = new ReverseGeocodingContext();
            GeoRegion region = new GeoRegion(latitude, longitude);
            if (radius != null)
                region.setRadius(radius);
            ctx.setSearchRegion(region);

            return new ReverseGeocodingResponse(ErrorCode.OK, gate.reverseGeocode(ctx));
        } else {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Parameters 'latitude' & 'longitude' must be not null");
        }
    }

    @GET
    @Path("/places/{place_id}")
    public Response getPlaceDetails(
            @PathParam("place_id") String placeId
    ) {
        if (StringUtils.isNotBlank(placeId)) {
            return new PlaceDetailsResponse(convert(gate.getPlaceDetails(placeId)));
        } else {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Parameter place_id must be set");
        }
    }

    private com.haulmont.shamrock.as.google.places.gate.rs.v1.dto.PlaceDetails convert(PlaceDetails place) {
        if(place==null || place.getAddressComponents() == null || place.getAddressComponents().isEmpty())
            return null;

        com.haulmont.shamrock.as.google.places.gate.rs.v1.dto.PlaceDetails res = new com.haulmont.shamrock.as.google.places.gate.rs.v1.dto.PlaceDetails();
        res.setAddressComponents(place.getAddressComponents().stream().map(this::convert).collect(Collectors.toList()));
        res.setFormattedAddress(place.getFormattedAddress());
        res.setPlaceId(place.getId());
        res.setTypes(place.getTypes());
        res.setName(place.getDisplayName()!=null ? place.getDisplayName().getText() : "");
        res.setGeometry(convert(place.getLocation()));

        return res;
    }

    private Geometry convert(LatLng latLng) {
        if (latLng==null)
            return null;

        com.haulmont.shamrock.as.google.places.gate.dto.Location location = new com.haulmont.shamrock.as.google.places.gate.dto.Location();
        location.setLat(latLng.getLatitude());
        location.setLng(latLng.getLongitude());
        Geometry geometry = new Geometry();
        geometry.setLocation(location);

        return geometry;
    }

    private AddressComponent convert(com.haulmont.shamrock.as.google.places.gate.dto.AddressComponent component) {
        AddressComponent res = new AddressComponent();
        res.setLongName(component.getLongText());
        res.setShortName(component.getShortText());
        res.setTypes(component.getTypes());
        return res;
    }
}