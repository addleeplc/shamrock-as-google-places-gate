/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.rs.v1;

import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.ServiceException;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.monaco.response.Response;
import com.haulmont.shamrock.as.context.AutocompleteContext;
import com.haulmont.shamrock.as.contexts.*;
import com.haulmont.shamrock.as.dto.Accuracy;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.dto.LatLon;
import com.haulmont.shamrock.as.dto.Location;
import com.haulmont.shamrock.as.google.gate.GoogleAddressSearchGate;
import com.haulmont.shamrock.as.google.gate.rs.v1.dto.GeocodeResponse;
import com.haulmont.shamrock.as.google.gate.rs.v1.dto.RefineResponse;
import com.haulmont.shamrock.as.google.gate.rs.v1.dto.ReverseGeocodingResponse;
import com.haulmont.shamrock.as.google.gate.rs.v1.dto.SearchResponse;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nikita Bozhko on 01.01.17.
 * Project Shamrock
 */

@Path("/v1")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class GoogleGateResource {

    public static final Pattern LOCATION_PATTERN = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+),([-+]?[0-9]*\\.?[0-9]+)");

    @GET
    @Path("search")
    public Response search(
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

            return new SearchResponse(ErrorCode.OK, getGate().search(ctx));
        } else {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Parameter 'search_string' must be non-null");
        }
    }

    @GET
    @Path("autocomplete")
    public Response autocomplete(
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

                    com.haulmont.shamrock.as.dto.Location l = new com.haulmont.shamrock.as.dto.Location();

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

            return new SearchResponse(ErrorCode.OK, getGate().autocomplete(ctx));
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

//    @GET
//    @Path("search/beneath")
//    public Response searchBeneath(
//            @QueryParam("search_flats") boolean searchFlats,
//            @QueryParam("search_business_names") boolean searchBusinessNames,
//            @QueryParam("start_index") Integer startIndex,
//            @QueryParam("max_results") Integer maxResults,
//            @QueryParam("flatten") boolean flatten,
//            @QueryParam("city") String city,
//            @QueryParam("preferred_city") String preferredCity,
//            @QueryParam("address_id") String addressId
//    ) {
//        if (StringUtils.isBlank(addressId))
//            throw new ServiceException(ErrorCode.BAD_REQUEST, "Parameter 'address_id' must be non-null");
//
//        SearchBeneathContext ctx = new SearchBeneathContext();
//
//        Address a = new Address();
//        a.setId(addressId);
//        ctx.setAddress(a);
//
//        ctx.setCity(city);
//        ctx.setSearchFlats(searchFlats);
//        ctx.setSearchBusinessNames(searchBusinessNames);
//
//        ctx.setStartIndex(startIndex);
//        ctx.setMaxResults(maxResults);
//        ctx.setFlatten(flatten);
//
//        return new SearchResponse(ErrorCode.OK, getGate().searchBeneath(ctx));
//    }

    @POST
    @Path("refine")
    public Response refine(
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
        if (StringUtils.containsIgnoreCase(id, getGate().getId())) {
            return new RefineResponse(ErrorCode.OK, getGate().refine(ctx));
        } else {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Address Id is invalid");
        }
    }

    @GET
    @Path("geocode")
    public Response geocode(
            @QueryParam("address") String address,
            @QueryParam("postcode") String postcode,
            @QueryParam("city") String city,
            @QueryParam("country") String country,
            @QueryParam("latitude") Double latitude,
            @QueryParam("longitude") Double longitude,
            @QueryParam("radius") Double radius,
            @QueryParam("accuracy") Accuracy accuracy
    ) {
        if (StringUtils.isBlank(address) && (latitude == null || longitude == null || radius == null))
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Parameter 'address' or parameters (latitude, longitude, radius) must be non-null.");

        GeocodeContext context = new GeocodeContext();
        context.setAddress(address);
        context.setPostcode(postcode);
        context.setCity(city);
        context.setCountry(country);

        Location location = new Location();
        location.setLat(latitude);
        location.setLon(longitude);
        //location.setAccuracy(accuracy);
        context.setLocation(location);

        context.setRadius(radius);

        return new GeocodeResponse(ErrorCode.OK, getGate().geocode(context));
    }

    @GET
    @Path("reverse-geocode")
    public Response reverseGeocode(
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

            return new ReverseGeocodingResponse(ErrorCode.OK, getGate().reverseGeocode(ctx));
        } else {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Parameters 'latitude' & 'longitude' must be not null");
        }
    }

    private GoogleAddressSearchGate getGate() {
        return AppContext.getBean(GoogleAddressSearchGate.class);
    }
}
