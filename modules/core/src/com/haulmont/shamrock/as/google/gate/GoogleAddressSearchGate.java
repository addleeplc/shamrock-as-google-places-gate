/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.jackson.ObjectReaderWriterFactory;
import com.haulmont.shamrock.address.Address;
import com.haulmont.shamrock.address.AddressSearchGate;
import com.haulmont.shamrock.address.GeocodeContext;
import com.haulmont.shamrock.address.context.RefineContext;
import com.haulmont.shamrock.address.context.ReverseGeocodingContext;
import com.haulmont.shamrock.address.context.SearchBeneathContext;
import com.haulmont.shamrock.address.context.SearchContext;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import org.picocontainer.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by Nikita Bozhko on 02.01.17.
 * Project Shamrock
 */

@Component
public class GoogleAddressSearchGate implements AddressSearchGate {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAddressSearchGate.class);

    static {
        Unirest.setObjectMapper(new JacksonObjectMapper());
    }

    private final AddressSearchGate delegator;

    public GoogleAddressSearchGate() {
        if (AppContext.getServiceName().contains("google-places")) {
            delegator = new GooglePlacesAddressSearchGate();
        } else if (AppContext.getServiceName().contains("google-geocode")) {
            delegator = new GoogleGeocodeAddressSearchGate();
        } else {
            delegator = null;
        }
    }

    @Override
    public String getId() {
        return delegator.getId();
    }

    @Override
    public List<Address> search(SearchContext context) {
        return delegator.search(context);
    }

    @Override
    public List<Address> searchBeneath(SearchBeneathContext context) {
        return delegator.searchBeneath(context);
    }

    @Override
    public Address refine(RefineContext context) {
        return delegator.refine(context);
    }

    @Override
    public Address geocode(GeocodeContext context) {
        return delegator.geocode(context);
    }

    @Override
    public List<Address> reverseGeocode(ReverseGeocodingContext context) {
        return delegator.reverseGeocode(context);
    }

    private static class JacksonObjectMapper implements ObjectMapper {
        private final ObjectReaderWriterFactory rw = AppContext.getBean(ObjectReaderWriterFactory.class);

        @Override
        public <T> T readValue(String value, Class<T> valueType) {
            try {
                if (logger.isDebugEnabled())
                    logger.debug("Response: \n{}", value);

                return rw.reader(valueType).readValue(value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String writeValue(Object value) {
            try {
                return rw.writer(value).writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
