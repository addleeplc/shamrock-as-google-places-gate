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
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.FormattableText;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.PlacePrediction;
import com.haulmont.shamrock.as.google.places.gate.utils.GoogleAddressSearchUtils;
import com.haulmont.shamrock.as.google.places.gate.utils.GoogleAddressUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class AutocompleteService extends AbstractSearchByTextService {

    @Inject
    private Logger logger;

    @Inject
    private GooglePlacesService googlePlacesService;

    @Inject
    private PlaceParsingService placeParsingService;

    @Inject
    private PlaceDetailsService placeDetailsService;

    //

    public List<Address> autocomplete(AutocompleteContext context) {
        String searchString = context.getSearchString();
        if (StringUtils.isEmpty(searchString)) return Collections.emptyList();

        long ts = System.currentTimeMillis();

        List<PlacePrediction> predictions = new ArrayList<>();

        if (StringUtils.isNotBlank(context.getCity())) {
            predictions.addAll(doSearch(context));
        } else {
            predictions.addAll(doSearch(context));

            if (!hasGoodMatches(predictions, context)
                    && StringUtils.isNotBlank(context.getPreferredCity()))
            {
                String country = StringUtils.isBlank(context.getCountry()) ? context.getPreferredCountry() : context.getCountry();
                String city = context.getPreferredCity();

                AutocompleteContext temp = GoogleAddressSearchUtils.clone(context);

                temp.setCity(city);
                temp.setCountry(country);

                predictions.addAll(doSearch(temp));
            }
        }

        List<Address> res = convert(predictions);
        logger.debug("Autocomplete address (text: '{}', resSize: {}) ({} ms)'", context.getSearchString(), res.size(), System.currentTimeMillis() - ts);

        return res;
    }

    private Collection<PlacePrediction> doSearch(AutocompleteContext context) {
        List<PlacePrediction> predictions = null;
        try {
            predictions = googlePlacesService.autocomplete(context);
        } catch (Exception e) {
            logger.warn("Failed to search address", e);
        }

        if (CollectionUtils.isEmpty(predictions)) return Collections.emptyList();

        return predictions.stream()
                .filter(this::isValid)
                .collect(Collectors.toList());
    }

    private boolean hasGoodMatches(List<PlacePrediction> predictions, AutocompleteContext context) {
        String country = GoogleAddressUtils.resolveRegionCode(context.getCountry() == null ? context.getPreferredCountry() : context.getCountry());
        String city = context.getCity() == null ? context.getPreferredCity() : context.getCity();

        for (PlacePrediction p : predictions) {
            if (p.getText() == null || StringUtils.isBlank(p.getText().getText())) continue;

            String formattedAddress = p.getText().getText();
            if ((StringUtils.isBlank(country) || containsWord(formattedAddress, country))
                    && (StringUtils.isBlank(city) || containsWord(formattedAddress, city)))
            {
                return true;
            }
        }

        return false;
    }

    private boolean containsWord(String text, String word) {
        if (StringUtils.isBlank(text) || StringUtils.isBlank(word)) return false;

        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }

    //

    @SuppressWarnings("RedundantIfStatement")
    private boolean isValid(PlacePrediction place) {
        if (place == null) return false;

        List<String> types = place.getTypes();
        if (CollectionUtils.isEmpty(types)) return false;

        if (GoogleAddressUtils.isArea(types)) return false;
        if (isFilterAirports() && GoogleAddressUtils.isAirport(types)) return false;

        return true;
    }

    private List<Address> convert(List<PlacePrediction> predictions) {
        if (predictions == null) return Collections.emptyList();

        List<Address> res = new ArrayList<>();

        for (PlacePrediction prediction : predictions) {
            Address address = convert(prediction);
            if (address != null) {
                res.add(address);
            }
        }

        return res;
    }

    private Address convert(PlacePrediction prediction) {
        boolean filterNonParsed = isFilterNonParsed();
        boolean callDetailsForNonParsed = isRefineNonParsed();

        Address address = tryParse(prediction);

        boolean parsed = isParsed(address);
        if (parsed) {
            return address;
        } else if (callDetailsForNonParsed) {
            return refine(address);
        } else if (!filterNonParsed) {
            return address;
        } else {
            return null;
        }
    }

    private Address tryParse(PlacePrediction prediction) {
        Address address;

        boolean parsingEnabled = isEnableParsing();
        if (parsingEnabled) {
            try {
                address = parse(prediction);
            } catch (Exception e) {
                logger.warn("Failed to parse address", e);
                address = null;
            }

            if (address == null) {
                address = asAddress(prediction);
            }
        } else {
            address = asAddress(prediction);
        }

        return address;
    }

    private Address parse(PlacePrediction prediction) {
        FormattableText predictionText = prediction.getText();
        if (predictionText == null) return null;

        Place place = new Place();

        place.setId(prediction.getPlaceId());
        place.setFormattedAddress(predictionText.getText());
        place.setTypes(prediction.getTypes());

        return placeParsingService.parse(place);
    }

    private static Address asAddress(PlacePrediction prediction) {
        if (prediction == null) return null;

        FormattableText predictionText = prediction.getText();
        if (predictionText == null) return null;

        Address res = new Address();

        res.setId(String.format("google-places|%s", prediction.getPlaceId()));

        AddressData data = new AddressData();
        data.setFormattedAddress(predictionText.getText());
        data.setAddressComponents(new AddressComponents());
        res.setAddressData(data);

        return res;
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
