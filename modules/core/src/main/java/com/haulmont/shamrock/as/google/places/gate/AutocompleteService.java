/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate;

import com.haulmont.shamrock.as.context.AutocompleteContext;
import com.haulmont.shamrock.as.contexts.RefineType;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.dto.AddressComponents;
import com.haulmont.shamrock.as.dto.AddressData;
import com.haulmont.shamrock.as.google.places.gate.dto.Place;
import com.haulmont.shamrock.as.google.places.gate.dto.RefineContext;
import com.haulmont.shamrock.as.google.places.gate.parsers.PlaceParsingService;
import com.haulmont.shamrock.as.google.places.gate.services.GooglePlacesService;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.PlacePrediction;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class AutocompleteService {

    @Inject
    private Logger logger;

    @Inject
    private GooglePlacesService googlePlacesService;

    @Inject
    private PlaceParsingService placeParsingService;

    @Inject
    private PlaceDetailsService placeDetailsService;

    @Inject
    private ServiceConfiguration configuration;

    //

    public List<Address> autocomplete(AutocompleteContext context) {
        long ts = System.currentTimeMillis();

        List<PlacePrediction> predictions = googlePlacesService.autocomplete(context);
        List<Address> res = convert(predictions);
        logger.debug("Autocomplete address (text: '{}', resSize: {}) ({} ms)'", context.getSearchString(), res.size(), System.currentTimeMillis() - ts);

        return res;
    }

    //

    private List<Address> convert(List<PlacePrediction> predictions) {
        if (predictions == null) return Collections.emptyList();

        List<Address> res = new ArrayList<>();

        boolean filterNonParsedAddresses = Optional.ofNullable(configuration.geFilterNonParsedAddressed()).orElse(Boolean.TRUE);
        boolean callDetailsForNonParsedAddressed = Optional.ofNullable(configuration.getCallDetailsForNonParsedAddressed()).orElse(Boolean.FALSE);

        for (PlacePrediction prediction : predictions) {
            Address address = convert(prediction);

            if (filterNonParsedAddresses) {
                AddressData data = address.getAddressData();
                if (data != null) {
                    AddressComponents components = data.getAddressComponents();
                    if (components != null && StringUtils.isNotBlank(components.getCountry())) {
                        res.add(address);
                    } else if (callDetailsForNonParsedAddressed) {
                        res.add(refine(address));
                    }
                } else {
                    res.add(refine(address));
                }
            } else if (callDetailsForNonParsedAddressed) {
                res.add(address);
            }
        }

        return res;
    }

    private Address convert(PlacePrediction prediction) {
        Place place = new Place();

        if (prediction.getText() != null)
            place.setFormattedAddress(prediction.getText().getText());
        place.setTypes(prediction.getTypes());
        place.setId(prediction.getPlaceId());

        Address address = placeParsingService.parse(place);
        if (address == null) {
            address = new Address();

            AddressData data = new AddressData();
            AddressComponents components = new AddressComponents();

            address.setId(String.format("google-places|%s", prediction.getPlaceId()));
            if (prediction.getText() != null)
                data.setFormattedAddress(prediction.getText().getText());

            data.setAddressComponents(components);
            address.setAddressData(data);
        }

        return address;
    }

    private Address refine(Address res) {
        RefineContext context = new RefineContext();
        context.setAddress(res);
        context.setRefineType(RefineType.DEFAULT);

        return refine(context);
    }


    public Address refine(RefineContext context) {
        return placeDetailsService.getDetails(context);
    }
}
