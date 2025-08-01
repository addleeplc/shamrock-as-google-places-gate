/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.utils;

import com.haulmont.shamrock.as.google.places.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.places.gate.dto.Place;
import com.haulmont.shamrock.as.google.places.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.places.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.LatLng;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public final class GoogleAddressUtils {

    public static Map<String, AddressComponent> convert(List<AddressComponent> components) {
        Map<String, AddressComponent> res = new HashMap<>();

        for (AddressComponent component : components) {
            for (String key : component.getTypes()) {
                if (key != null) {
                    res.put(key, component);
                }
            }
        }

        return res;
    }

    public static String getFirstShort(Map<String, AddressComponent> components, GElement... elements) {
        return getFirst(components, false, elements);
    }

    public static String getFirstLong(Map<String, AddressComponent> components, GElement... elements) {
        return getFirst(components, true, elements);
    }

    private static String getFirst(Map<String, AddressComponent> components, boolean isGetLongName, GElement... elements) {
        if (elements != null) {
            for (GElement element : elements) {
                AddressComponent component = components.get(element.toString());

                if (component != null) {

                    if (isGetLongName) {
                        if (StringUtils.isNotBlank(component.getLongText())) {
                            return component.getLongText().trim();
                        }
                    } else {
                        if (StringUtils.isNotBlank(component.getShortText())) {
                            return component.getShortText().trim();
                        }
                    }
                }
            }

        }
        return null;
    }

    public static com.haulmont.shamrock.as.dto.Location convert(LatLng latLng) {
        if (latLng != null) {
            com.haulmont.shamrock.as.dto.Location l = new com.haulmont.shamrock.as.dto.Location();
            l.setLat(latLng.getLatitude());
            l.setLon(latLng.getLongitude());

            return l;
        }

        return null;
    }

    public static String getFormattedAddress(Place place) {
        String formattedAddress;
        if (place instanceof PlaceDetails)
            formattedAddress = makeupFormattedAddress(((PlaceDetails) place).getAddressComponents());
        else
            formattedAddress = place.getFormattedAddress();

        if (isBuilding(place))
            return formattedAddress;
        else
            return concat(place.getDisplayName() != null ? place.getDisplayName().getText() : "", formattedAddress);
    }



    private static String makeupFormattedAddress(List<AddressComponent> components) {
        StringBuilder sb = new StringBuilder();

        sb.append(getAddressComponentByName(components, "premise"));
        sb.append(", ");
        sb.append(getAddressComponentByName(components, "street_number"));
        sb.append(" ");
        sb.append(getAddressComponentByName(components, "route"));
        sb.append(", ");
        sb.append(getAddressComponentByName(components, "postal_town"));
        sb.append(", ");
        sb.append(getAddressComponentByName(components, "postal_code"));

        return sb.toString().trim();
    }

    private static String getAddressComponentByName(List<AddressComponent> components, String name) {
        if (components == null || StringUtils.isBlank(name)) return null;

        for( AddressComponent component : components)
            if (component.getTypes().contains(name))
                return component.getLongText();

        return "";
    }

    private static String concat(String name, String address) {
        return StringUtils.isBlank(name) ? address : (name + ", " + address);
    }

    public static boolean isBuilding(Place place) {
        List<String> types = place.getTypes();
        return isBuilding(types);
    }

    public static boolean isBuilding(List<String> types) {
        if (types == null) return false;

        return CollectionUtils.containsAny(types, Arrays.asList(GElement.street_address.name(), GElement.route.name(), GElement.premise.name(), GElement.subpremise.name()));
    }

    public static boolean isArea(List<String> types) {
        return CollectionUtils.size(types) == 1 &&
                CollectionUtils.containsAny(
                        types,
                        Arrays.asList(
                                GElement.postal_code.name(),
                                GElement.postal_town.name(),
                                GElement.postal_code_prefix.name(),
                                GElement.postal_code_suffix.name())
                );
    }

    public static boolean isAirport(Collection<String> types) {
        Optional<String> o = types.stream()
                .filter(t -> t.equals(GElement.airport.name()))
                .findFirst();

        return o.isPresent();
    }

    public static String resolveRegionCode(String countryCode) {
        if (countryCode == null || countryCode.isEmpty())
            return null;

        if(countryCode.equalsIgnoreCase("GB"))
            return "UK";
        return countryCode.toUpperCase();
    }
}
