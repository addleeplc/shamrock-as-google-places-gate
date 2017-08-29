/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.rs.v1;

import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.monaco.response.Response;
import com.haulmont.shamrock.address.Accuracy;
import com.haulmont.shamrock.address.Address;
import com.haulmont.shamrock.address.AddressSearchGate;
import com.haulmont.shamrock.address.context.*;
import com.haulmont.shamrock.as.google.gate.rs.v1.dto.RefineResponse;
import com.haulmont.shamrock.as.google.gate.rs.v1.dto.ReverseGeocodingResponse;
import com.haulmont.shamrock.as.google.gate.rs.v1.dto.SearchResponse;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikita Bozhko on 01.01.17.
 * Project Shamrock
 */

@Path("/v1")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class GoogleGateResource {
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

            List<Address> res = new ArrayList<>();
            res.addAll(getGate().search(ctx));

            return new SearchResponse(ErrorCode.OK, res);
        } else {
            return new Response(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
        }
    }

    @GET
    @Path("search/beneath")
    public Response searchBeneath(
            @QueryParam("search_flats") boolean searchFlats,
            @QueryParam("search_business_names") boolean searchBusinessNames,
            @QueryParam("start_index") Integer startIndex,
            @QueryParam("max_results") Integer maxResults,
            @QueryParam("flatten") boolean flatten,
            @QueryParam("city") String city,
            @QueryParam("preferred_city") String preferredCity,
            @QueryParam("address_id") String addressId
    ) {
        throw new UnsupportedOperationException();
    }

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
            return new Response(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
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
        throw new UnsupportedOperationException("Unsupported for " + getGate().getId() + " gate");
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
            return new Response(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
        }
    }

    private AddressSearchGate getGate() {
        return AppContext.getBean(AddressSearchGate.class);
    }
}
