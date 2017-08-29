/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haulmont.shamrock.address.*;
import com.haulmont.shamrock.address.context.GeoRegion;
import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.Coordinates;
import com.haulmont.shamrock.as.google.gate.dto.Geometry;
import com.haulmont.shamrock.as.google.gate.dto.PlaceDetailsResult;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleAddressUtils {

    public static class AddressParseException extends Exception {
        public AddressParseException(String message) {
            super(message);
        }
    };

    public static Address parseAddress(String formattedAddress,
                                       String reqCountry, Geometry geometry,
                                       Map<String, AddressComponent> components,
                                       List<String> types) throws AddressParseException {

        if (!Constants.Country.isoToCountry.containsKey(reqCountry)) {
            throw new UnsupportedOperationException("Unknown country code: " + reqCountry);
        } else {
            // prepare address components
            sanitizeAddress(components);

            // country

            String countryValue = getFirstShort(components, GElement.country, GElement.political);
            if (StringUtils.isBlank(countryValue)) {
                throw new AddressParseException("country is null");
            } else if (StringUtils.isNotBlank(reqCountry) && !StringUtils.equals(countryValue, reqCountry)) {
                throw new AddressParseException("wrong country " + countryValue);
            }

            // city

            String cityValue;
            if ("GB".equals(countryValue)) {
                cityValue = getFirstLong(components, GElement.administrative_area_level_2);

                if (StringUtils.isNotBlank(cityValue) && StringUtils.equalsIgnoreCase(cityValue, "Greater London")) {
                    cityValue = "London";
                } else {
                    cityValue = getFirstLong(components, GElement.locality, GElement.postal_town);
                    if (StringUtils.isBlank(cityValue))
                        cityValue = getFirstLong(components, GElement.locality, GElement.political);

                    if (cityValue != null) {
                        if (cityValue.equalsIgnoreCase("Greater London")) {
                            cityValue = "London";
                        }
                    } else {
                        cityValue = getFirstLong(components, GElement.administrative_area_level_1);
                    }
                }
            } else if ("FR".equals(countryValue)) {
                // detect town by department
                String postcode = getFirstLong(components, GElement.postal_code);

                if (startsWithAny(postcode,
                        "75", // Paris
                        "92", // Hauts-de-Seine
                        "93", // Seine-Saint-Denis
                        "94" // Val-de-Marne
                        // "95"  // Val-d'Oise
                )) {
                    cityValue = "Paris";
                } else {
                    cityValue = getFirstLong(components, GElement.postal_town, GElement.locality);
                }
            } else if ("US".equals(countryValue)) {
                cityValue = getFirstLong(components, GElement.locality, GElement.postal_town, GElement.sublocality, GElement.neighborhood);
                if (cityValue != null &&
                        (cityValue.equals("NY") ||
                                "Manhattan".equalsIgnoreCase(cityValue) ||
                                "Brooklyn".equalsIgnoreCase(cityValue) ||
                                "Queens".equalsIgnoreCase(cityValue) ||
                                "Staten Island".equalsIgnoreCase(cityValue) ||
                                "The Bronx".equalsIgnoreCase(cityValue) ||
                                "Bronx".equalsIgnoreCase(cityValue))
                        ) {
                    cityValue = "New York";
                }

            } else if ("BE".equals(countryValue)) {
                cityValue = getFirstLong(components, GElement.administrative_area_level_1, GElement.locality);
                // ar1 = town, locality - town or towns region
            } else if ("IE".equals(countryValue)) {
                cityValue = getFirstLong(components, GElement.administrative_area_level_1, GElement.administrative_area_level_2, GElement.locality);
            } else if ("SE".equals(countryValue)) {
                cityValue = getFirstLong(components, GElement.postal_town, GElement.locality);
            } else {
                cityValue = getFirstLong(components, GElement.locality, GElement.postal_town);
            }

            if (StringUtils.isBlank(cityValue)) {
                throw new AddressParseException("town is null");
            }


            // postcode
            String postcode = null;
            if ("IE".equals(countryValue)) {
                // In general, postcodes are not required in Ireland (they doesn't have actual postcode system).
                // But in Dublin and Cork there a 1 or 2 digit zone number appears after the name of the city (eg 'Dublin 2'),
                // that value stored in postal_town field. Outside the city, it is simply County Dublin.
                String postalTown = getFirstLong(components, GElement.postal_town);
                if (StringUtils.isNotBlank(postalTown) && !StringUtils.equals(postalTown, cityValue)) {
                    String[] values = postalTown.split(" ");
                    postcode = values[values.length - 1];
                } else {
                    if ("Dublin".equals(cityValue)) {
                        postcode = "County";
                    }
                }
            } else if ("US".equals(countryValue) || "CA".equals(countryValue)) {
                postcode = getFirstLong(components, GElement.postal_code);
                String stateValue = getFirstShort(components, GElement.administrative_area_level_1);

                if (StringUtils.isNotBlank(postcode) && StringUtils.isNotBlank(stateValue)) {
                    postcode = stateValue + " " + postcode;
                }
            } else {
                postcode = getFirstLong(components, GElement.postal_code);
            }

            if (StringUtils.isBlank(postcode)) {
                throw new AddressParseException("postcode is null");
            }


            // city region
            String cityRegion = null;
            if ("US".equals(countryValue)) {
                cityRegion = getFirstLong(components, GElement.sublocality, GElement.sublocality_level_1);

                if ("New York".equals(cityValue) && StringUtils.isNotBlank(cityRegion)) {
                    if ("Manhattan".equalsIgnoreCase(cityRegion)) {
                        cityRegion = "Manhattan";
                    } else if ("Brooklyn".equalsIgnoreCase(cityRegion)) {
                        cityRegion = "Brooklyn";
                    } else if ("Queens".equalsIgnoreCase(cityRegion)) {
                        cityRegion = "Queens";
                    } else if ("Staten Island".equalsIgnoreCase(cityRegion)) {
                        cityRegion = "Staten Island";
                    } else if ("The Bronx".equalsIgnoreCase(cityValue) || "Bronx".equalsIgnoreCase(cityValue)) {
                        cityRegion = "Bronx";
                    }
                }
            }


            // address
            AddressComponents ac = new AddressComponents();
            ac.setCity(cityValue);
            ac.setCountry(countryValue);
            ac.setPostcode(postcode);

            String buildingNumber = getFirstLong(components, GElement.street_number);
            ac.setBuildingNumber(buildingNumber);

            String streetName = getFirstLong(components, GElement.route);
            ac.setStreet(streetName);

            String publicTransportStop = getFirstLong(components, GElement.train_station, GElement.transit_station, GElement.bus_station, GElement.subway_station);

            if (StringUtils.isNotBlank(publicTransportStop)) {
                ac.setAddress(publicTransportStop);
            } else {
                String poi = getFirstLong(components, GElement.airport, GElement.park, GElement.point_of_interest);

                if (poi != null) {
                    ac.setAddress(poi);

                } else if (isType(types, GType.intersection)) {
                    String interValue;
                    interValue = StringUtils.substringBefore(formattedAddress, ",");
                    if (StringUtils.isBlank(interValue))
                        interValue = formattedAddress;
                    ac.setAddress(interValue);

                } else {
                    if (ac.getStreet() == null) {
                        ac.setAddress(AddressHelper.parseBuildingAddress(formattedAddress));
                        ac.setStreet(AddressHelper.parseStreetName(formattedAddress, AddressHelper.ParseAccuracy.HIGH));

                        if (ac.getAddress() == null) {
                            // last chance
                            ac.setAddress(getFirstLong(components, GElement.establishment));
                            if (ac.getAddress() == null) {
                                ac.setAddress(ac.getStreet());
                            }
                        }

                    } else {
                        if (ac.getBuildingNumber() != null) {
                            ac.setAddress(ac.getBuildingNumber() + " " + ac.getStreet());
                        } else {
                            ac.setAddress(ac.getStreet());
                        }
                    }

                    if ("US".equals(ac.getCountry()) && cityRegion != null) {
                        ac.setAddress(ac.getAddress() + ", " + cityRegion);
                    }
                }
            }

            if (ac.getAddress() == null) {
                throw new AddressParseException("address is null");
            }

            AddressData ad = new AddressData();
            ad.setFormattedAddress(ac.getAddress() + ", " + ac.getCity() + ", " + ac.getPostcode());

            // geometry
            if (geometry != null) {
                Coordinates location = geometry.getLocation();
                if (location != null) {
                    Location l = new Location();
                    l.setLat(location.getLat());
                    l.setLon(location.getLng());

                    ad.setLocation(l);
                }
            }

            ad.setAddressComponents(ac);

            Address res = new Address();
            res.setType(AddressType.ADDRESS);
            res.setAddressData(ad);

            return res;
        }
    }


    private static void sanitizeAddress(Map<String, AddressComponent> components) {

        for (Map.Entry<String, AddressComponent> entry : components.entrySet()) {
            if (entry.getValue() != null) {
                String longName = sanitizeChars(entry.getValue().getLong_name());
                String shortName = sanitizeChars(entry.getValue().getShort_name());

                entry.getValue().setLong_name(longName);
                entry.getValue().setShort_name(shortName);
            }
        }
    }

    private static String sanitizeChars(String name) {
        if (name == null) {
            return null;
        } else {
            return name
                    .replaceAll("–", "-") // EN DASH, &#x2013
                    .replaceAll("Œ", "OE")
                    .replaceAll("œ", "oe")
                    .replaceAll("Ÿ", "Y")
                    .replaceAll("Ĳ", "IJ")
                    .replaceAll("ĳ", "ij");
        }
    }

    private static String getFirstLong(Map<String, AddressComponent> components, GElement... elements) {
        return getFirst(components, true, elements);
    }

    private static String getFirstShort(Map<String, AddressComponent> components, GElement... elements) {
        return getFirst(components, false, elements);
    }

    private static String getFirst(Map<String, AddressComponent> components, boolean isGetLongName, GElement... elements) {

        if (elements == null || elements.length == 0) {
            return null;
        } else {
            for (GElement element : elements) {
                AddressComponent component = components.get(element.toString());

                if (component != null) {

                    if (isGetLongName) {
                        if (StringUtils.isNotBlank(component.getLong_name())) {
                            return component.getLong_name().trim();
                        }
                    } else {
                        if (StringUtils.isNotBlank(component.getShort_name())) {
                            return component.getShort_name().trim();
                        }
                    }
                }
            }

            return null;
        }
    }

    private static boolean startsWithAny(String base, String... values) {
        if (StringUtils.isBlank(base)) {
            return false;
        } else {
            for (String value : values) {
                if (base.startsWith(value)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static boolean isType(List<String> types, GType value) {
        if (types == null || types.isEmpty() || value == null) {
            return false;
        } else {
            for (String type : types) {
                if (value.toString().equals(type)) {
                    return true;
                }
            }
            return false;
        }
    }

    private enum GElement {
        // MAIN
        street_address, // indicates a precise street address.
        route, // indicates a named route (such as "US 101").
        intersection, //indicates a major intersection, usually of two major roads.
        political, // indicates a political entity. Usually, this type indicates a polygon of some civil administration.
        country, // indicates the national political entity, and is typically the highest order type returned by the Geocoder.
        administrative_area_level_1, // indicates a first-order civil entity below the country level. Within the United States, these administrative levels are states. Not all nations exhibit these administrative levels.
        administrative_area_level_2, // indicates a second-order civil entity below the country level. Within the United States, these administrative levels are counties. Not all nations exhibit these administrative levels.
        administrative_area_level_3, //indicates a third-order civil entity below the country level. This type indicates a minor civil division. Not all nations exhibit these administrative levels.
        colloquial_area, //indicates a commonly-used alternative name for the entity.
        locality, // indicates an incorporated city or town political entity.
        sublocality, //indicates a first-order civil entity below a locality. For some locations may receive one of the additional types: sublocality_level_1 through to sublocality_level_5. Each sublocality level is a civil entity. Larger numbers indicate a smaller geographic area.
        sublocality_level_1, // see above
        neighborhood, //indicates a named neighborhood
        premise, //indicates a named location, usually a building or collection of buildings with a common name
        subpremise, //indicates a first-order entity below a named location, usually a singular building within a collection of buildings with a common name
        postal_code, // indicates a postal code as used to address postal mail within the country.
        natural_feature, // indicates a prominent natural feature.
        airport, // indicates an airport.
        park, // indicates a named park.
        point_of_interest, // indicates a named point of interest. Typically, these "POI"s are prominent local entities that don't easily fit in another category such as "Empire State Building" or "Statue of Liberty."

        // ADDITIONAL
        floor, // indicates the floor of a building address.
        establishment, // typically indicates a place that has not yet been categorized.
        parking, //indicates a parking lot or parking structure.
        post_box, //indicates a specific postal box.
        postal_town, //indicates a grouping of geographic areas, such as locality and sublocality, used for mailing addresses in some countries.
        room, // indicates the room of a building address.
        street_number, //indicates the precise street number.
        bus_station, //indicate the location of a bus, train or public transit stop.
        train_station,
        transit_station,
        subway_station // undocumented
    }

    private enum GType {
        intersection
    }

    //

    public static GeoRegion getSearchRegion(String countryCode) {
        GeoRegion region = null;
        try {
            if (StringUtils.isBlank(countryCode) || "GB".equals(countryCode)) {
                region = new GeoRegion(51.513977, -0.131149); // London
                region.setRadius(12000.0);
            } else if ("FR".equals(countryCode)) {
                region = new GeoRegion(48.856074, 2.352003); // Paris
                region.setRadius(15000.0);
            } else if ("ES".equals(countryCode)) {
                region = new GeoRegion(40.428577, -3.714455); // Madrid
                region.setRadius(13000.0);
            } else if ("US".equals(countryCode)) {
                region = new GeoRegion(40.7142700, -74.0059700); // New York
                region.setRadius(15000.0);
            } else if ("DE".equals(countryCode)) {
                region = new GeoRegion(52.5243700, 13.4105300); // Berlin
                region.setRadius(15000.0);
            } else if ("CH".equals(countryCode)) {
                region = new GeoRegion(47.382253, 8.535275); // Zurich
                region.setRadius(15000.0);
            } else if ("NL".equals(countryCode)) {
                region = new GeoRegion(52.370216, 4.895168); // Amsterdam
                region.setRadius(15000.0);
            } else if ("IT".equals(countryCode)) {
                region = new GeoRegion(41.872389, 12.480180); // Rome
                region.setRadius(15000.0);
            } else if ("IE".equals(countryCode)) {
                region = new GeoRegion(53.349805, -6.260310); // Dublin
                region.setRadius(15000.0);
            } else if ("SE".equals(countryCode)) {
                region = new GeoRegion(59.329323, 18.068581); // Stockholm
                region.setRadius(15000.0);
            } else if ("DK".equals(countryCode)) {
                region = new GeoRegion(55.676097, 12.568337); // Copenhagen
                region.setRadius(15000.0);
            } else if ("BE".equals(countryCode)) {
                region = new GeoRegion(50.850000, 4.350000); // Brussels
                region.setRadius(15000.0);
            } else if ("CA".equals(countryCode)) {
                region = new GeoRegion(43.715103, -79.382984); // Toronto
                region.setRadius(15000.0);
            } else {
                throw new UnsupportedOperationException("Unknown country code: " + countryCode);
            }
        } catch (Throwable e) {
            region = new GeoRegion(51.513977, -0.131149);
            region.setRadius(12000.0);
        }

        return region;
    }

    public static void assignPlaceDetails(Address address, PlaceDetailsResult details) {
        String name = details.getName();
        if (StringUtils.isNotBlank(name) && isCompanyName(address, name)) {
            address.getAddressData().getAddressComponents().setCompany(name);
            address.getAddressData().setFormattedAddress(name + ", " + address.getAddressData().getFormattedAddress());
            address.getAddressData().getAddressComponents().setAddress(name + ", " + address.getAddressData().getAddressComponents().getAddress());
        }
    }

    private static boolean isCompanyName(Address address, String name) {
        if (StringUtils.startsWith(address.getAddressData().getAddressComponents().getAddress(), name)) return false;

        String[] nComponents = name.split(" ");

        int idx = address.getAddressData().getAddressComponents().getAddress().indexOf(",");
        String[] aComponents = (idx < 0 ?
                address.getAddressData().getAddressComponents().getAddress() :
                address.getAddressData().getAddressComponents().getAddress().substring(0, idx)
        ).split(" ");

        if (nComponents.length != aComponents.length) return true;
        int i = 0;
        for (String aComponent : aComponents) {
            if (StringUtils.startsWith(aComponent, nComponents[i])) return false;
            i++;
        }

        return true;
    }

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

    public static <T> T parseResponse(Class<T> responseClass, InputStream inputStream) throws IOException {
        StringBuilder jsonResult = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

            String line;
            while ((line = reader.readLine()) != null) {
                jsonResult.append(line).append("\n");
            }
        } finally {
            inputStream.close();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        T response = mapper.readValue(jsonResult.toString(), responseClass);
        return response;
    }
}
