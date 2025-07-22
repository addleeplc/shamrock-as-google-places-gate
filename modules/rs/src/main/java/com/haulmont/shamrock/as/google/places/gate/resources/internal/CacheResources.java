/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.resources.internal;

import com.haulmont.monaco.response.ErrorCode;
import com.haulmont.monaco.response.Response;
import com.haulmont.shamrock.as.google.places.gate.cache.SearchResCache;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/internal/caches")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class CacheResources {

    @Inject
    private SearchResCache searchResCache;

    //

    @GET
    @Path("/invalidate-all")
    public Response invalidateAll() {
        return __invalidateAll();
    }

    @POST
    @Path("/invalidate-all")
    public Response postInvalidateAll() {
        return __invalidateAll();
    }

    //

    private Response __invalidateAll() {
        searchResCache.invalidateAll();
        return new Response(ErrorCode.OK);
    }
}
