/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.utils;

import com.haulmont.monaco.AppContext;
import com.haulmont.shamrock.address.*;
import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.as.google.gate.GateConfiguration;
import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.Geometry;
import com.haulmont.shamrock.as.google.gate.dto.Location;
import com.haulmont.shamrock.as.google.gate.dto.PlaceDetailsResult;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public final class GoogleAddressUtils {

    private static final Map<String, String> elementsSpecifics = new HashMap<>();

    static {
        elementsSpecifics.put(GElement.airport.name(), "Airports/Airfields");

        elementsSpecifics.put(GElement.train_station.name(), "Train Station");
        elementsSpecifics.put(GElement.subway_station.name(), "Tube Station");
        elementsSpecifics.put(GElement.bus_station.name(), "Coach Station");

        elementsSpecifics.put(GElement.school.name(), "Schools/College's");
        elementsSpecifics.put(GElement.university.name(), "Schools/College's");

        elementsSpecifics.put(GElement.hospital.name(), "Hospitals");
        elementsSpecifics.put(GElement.health.name(), "Hospitals");
        elementsSpecifics.put(GElement.dentist.name(), "Hospitals");
        elementsSpecifics.put(GElement.physiotherapist.name(), "Hospitals");
        elementsSpecifics.put(GElement.doctor.name(), "Hospitals");

        elementsSpecifics.put(GElement.clothing_store.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.shoe_store.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.book_store.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.bicycle_store.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.home_goods_store.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.convenience_store.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.department_store.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.electronics_store.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.furniture_store.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.hardware_store.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.jewelry_store.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.liquor_store.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.pet_store.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.store.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.shopping_mall.name(), "Shopping Centres");
        elementsSpecifics.put(GElement.grocery_or_supermarket.name(), "Shopping Centres");

        elementsSpecifics.put(GElement.restaurant.name(), "Restaurant/Bar/Pub");
        elementsSpecifics.put(GElement.cafe.name(), "Restaurant/Bar/Pub");
        elementsSpecifics.put(GElement.bar.name(), "Restaurant/Bar/Pub");

        elementsSpecifics.put(GElement.zoo.name(), "Zoo");
    }

    private static final Map<String, AddressType> elementsTypes = new HashMap<>();

    static {
        elementsTypes.put(GElement.airport.name(), AddressType.airport);

        elementsTypes.put(GElement.train_station.name(), AddressType.train_station);
        elementsTypes.put(GElement.subway_station.name(), AddressType.subway_station);
        elementsTypes.put(GElement.bus_station.name(), AddressType.bus_station);

        elementsTypes.put(GElement.school.name(), AddressType.school);
        elementsTypes.put(GElement.university.name(), AddressType.university);

        elementsTypes.put(GElement.hospital.name(), AddressType.hospital);
        elementsTypes.put(GElement.health.name(), AddressType.hospital);
        elementsTypes.put(GElement.dentist.name(), AddressType.hospital);
        elementsTypes.put(GElement.physiotherapist.name(), AddressType.hospital);
        elementsTypes.put(GElement.doctor.name(), AddressType.hospital);

        elementsTypes.put(GElement.clothing_store.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.shoe_store.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.book_store.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.bicycle_store.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.home_goods_store.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.convenience_store.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.department_store.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.electronics_store.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.furniture_store.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.hardware_store.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.jewelry_store.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.liquor_store.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.pet_store.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.store.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.shopping_mall.name(), AddressType.shopping_centre);
        elementsTypes.put(GElement.grocery_or_supermarket.name(), AddressType.shopping_centre);

        elementsTypes.put(GElement.police.name(), AddressType.police);

        elementsTypes.put(GElement.night_club.name(), AddressType.night_club);

        elementsTypes.put(GElement.stadium.name(), AddressType.stadium);

        elementsTypes.put(GElement.lodging.name(), AddressType.hotel);

        elementsTypes.put(GElement.restaurant.name(), AddressType.restaurant);
        elementsTypes.put(GElement.cafe.name(), AddressType.restaurant);
        elementsTypes.put(GElement.bar.name(), AddressType.restaurant);

        elementsTypes.put(GElement.museum.name(), AddressType.museum);
    }

    private GoogleAddressUtils() {
    }

    public static class AddressParseException extends Exception {
        public AddressParseException(String message) {
            super(message);
        }
    }

    public static Address parseAddress(String formattedAddress, Geometry geometry, Map<String, AddressComponent> components, List<String> types) throws AddressParseException {
        if (isAirport(types))
            return null;

        // prepare address components
        sanitizeAddress(components);

        // country
        String countryValue = getFirstShort(components, GElement.country, GElement.political);
        if (StringUtils.isBlank(countryValue)) {
            throw new AddressParseException("country is null");
        }

        if ("RU".equals(countryValue)) {
            ru_transliterateComponents(components);
        } else if ("BG".equals(countryValue)) {
            bg_transliterateComponents(components);
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
            if (StringUtils.containsIgnoreCase(cityValue, "county dublin")) {
                cityValue = "Dublin";
            }
        } else if ("SE".equals(countryValue)) {
            cityValue = getFirstLong(components, GElement.postal_town, GElement.locality);
        } else if ("HK".equals(countryValue)) {
            cityValue = "Hong Kong";
        } else if ("JP".equals(countryValue)) {
            cityValue = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
            cityValue = cityValue.replace("-to", "");
        } else if ("CN".equals(countryValue)) {
            cityValue = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
        } else if ("IN".equals(countryValue)) {
            cityValue = getFirstLong(components, GElement.locality, GElement.political);
            if (StringUtils.isBlank(cityValue))
                cityValue = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);

            if ("New Delhi".equals(cityValue))
                cityValue = "Delhi";
        } else if ("TR".equals(countryValue)) {
            cityValue = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
        } else if ("CZ".equals(countryValue)) {
            cityValue = getFirstLong(components, GElement.sublocality_level_1, GElement.sublocality, GElement.political);
            if (cityValue.matches("Praha\\s[0-9]+")) {
                cityValue = "Prague";
            } else {
                cityValue = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
                if ("Hlavní město Praha".equals(cityValue))
                    cityValue = "Prague";
            }
        } else if ("TW".equals(countryValue)) {
            cityValue = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
        } else if ("ID".equals(countryValue)) {
            cityValue = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
        } else if ("VN".equals(countryValue)) {
            cityValue = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
        } else if ("EG".equals(countryValue)) {
            cityValue = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
            if ("Cairo Governorate".equals(cityValue)) {
                cityValue = "Cairo";
            }
        } else if ("SK".equals(countryValue)) {
            cityValue = getFirstLong(components, GElement.sublocality_level_1, GElement.sublocality, GElement.political);
            if (StringUtils.isNotBlank(cityValue) && StringUtils.equalsAnyIgnoreCase(
                    cityValue,
                    "Ružinov", "Nové Mesto", "Devínska Nová Ves", "Staré Mesto", "Podunajské Biskupice",
                    "Vrakuňa", "Rača", "Vajnory", "Devín", "Dúbravka", "Karlova Ves", "Lamač", "Záhorská Bystrica",
                    "Čunovo", "Jarovce", "Petržalka", "Rusovce")
                )
                cityValue = "Bratislava";
        } else if ("TH".equals(countryValue)) {
            cityValue = getFirstLong(components, GElement.administrative_area_level_1, GElement.political);
            if ("Krung Thep Maha Nakhon".equals(cityValue) || "Krung Thep".equals(cityValue))
                cityValue = "Bangkok";
        } else {
            cityValue = getFirstLong(components, GElement.locality, GElement.postal_town);
        }

        if (StringUtils.isBlank(cityValue)) {
            throw new AddressParseException("town is null");
        }

        // postcode
        String postcode = null;
        if ("IE".equals(countryValue)) {
            postcode = getFirstLong(components, GElement.postal_code);
            if (StringUtils.isBlank(postcode)) {
                postcode = null;
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

        if (StringUtils.isBlank(postcode) && getCountriesRequiresPostcode().contains(countryValue)) {
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

        String buildingName = getFirstLong(components, GElement.premise, GElement.subpremise);
        ac.setBuildingName(buildingName);

        String buildingNumber = getFirstLong(components, GElement.street_number);
        ac.setBuildingNumber(buildingNumber);

        String streetName = getFirstLong(components, GElement.route);
        if ("RU".equals(countryValue) || "BG".equals(countryValue)) {
            streetName = streetName.replace("ulitsa", "")
                    .replace("ul.", "")
                    .trim();
        }
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

                    if (StringUtils.isBlank(ac.getStreet()))
                        ac.setStreet(AddressHelper.parseStreetName(formattedAddress, AddressHelper.ParseAccuracy.LOW));

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
        if (StringUtils.isNotBlank(ac.getPostcode()))
            ad.setFormattedAddress(ac.getAddress() + ", " + ac.getCity() + ", " + ac.getPostcode());
        else
            ad.setFormattedAddress(ac.getAddress() + ", " + ac.getCity());

        if (StringUtils.isNotBlank(ac.getBuildingName())) {
            if (!StringUtils.containsIgnoreCase(ac.getAddress(), ac.getBuildingName()))
                ac.setAddress(ac.getBuildingName() + ", " + ac.getAddress());
            ad.setFormattedAddress(ac.getBuildingName() + ", " + ad.getFormattedAddress());
        }

        // geometry
        if (geometry != null) {
            Location location = geometry.getLocation();
            if (location != null) {
                com.haulmont.shamrock.address.Location l = new com.haulmont.shamrock.address.Location();
                l.setLat(location.getLat());
                l.setLon(location.getLng());

                ad.setLocation(l);
            }
        }

        ad.setAddressComponents(ac);

        Address res = new Address();
        res.setType(ItemType.ADDRESS);
        res.setAddressData(ad);

        String specifics = getAddressSpecifics(types);
        if (specifics != null) {
            AddressDetails details = new AddressDetails();
            details.setSpecifics(specifics);

            res.setDetails(details);
        }

        AddressType type = getAddressType(types);
        if (type != null) {
            if (res.getDetails() == null)
                res.setDetails(new AddressDetails());

            res.getDetails().setType(type);
        }

        return res;
    }

    private static boolean isAirport(Collection<String> types) {
        Optional<String> o = types.stream()
                .filter(t -> t.equals(GElement.airport.name()))
                .findFirst();

        return o.isPresent();
    }

    private static String getAddressSpecifics(Collection<String> types) {
        Optional<String> o = types.stream()
                .filter(elementsSpecifics::containsKey)
                .map(elementsSpecifics::get)
                .findFirst();

        return o.orElse(null);
    }

    private static AddressType getAddressType(Collection<String> types) {
        Optional<AddressType> o = types.stream()
                .filter(elementsTypes::containsKey)
                .map(elementsTypes::get)
                .findFirst();

        return o.orElse(null);
    }

    private static Set<String> getCountriesRequiresPostcode() {
        GateConfiguration conf = AppContext.getConfig().get(GateConfiguration.class);

        String s = conf.getCountriesRequiresPostcode();
        if (StringUtils.isBlank(s))
            return Collections.emptySet();

        s = StringUtils.deleteWhitespace(s);

        return new HashSet<>(Arrays.asList(s.split("[;,]")));
    }

    private static void ru_transliterateComponents(Map<String, AddressComponent> components) {
        for (Map.Entry<String, AddressComponent> entry : components.entrySet()) {
            if (entry.getValue() != null) {
                String longName = TransliterationUtils.ru_transliterate(entry.getValue().getLongName());
                String shortName = TransliterationUtils.ru_transliterate(entry.getValue().getShortName());

                entry.getValue().setLongName(longName);
                entry.getValue().setShortName(shortName);
            }
        }
    }

    private static void bg_transliterateComponents(Map<String, AddressComponent> components) {
        for (Map.Entry<String, AddressComponent> entry : components.entrySet()) {
            if (entry.getValue() != null) {
                String longName = TransliterationUtils.bg_transliterate(entry.getValue().getLongName());
                String shortName = TransliterationUtils.bg_transliterate(entry.getValue().getShortName());

                entry.getValue().setLongName(longName);
                entry.getValue().setShortName(shortName);
            }
        }
    }

    private static void sanitizeAddress(Map<String, AddressComponent> components) {

        for (Map.Entry<String, AddressComponent> entry : components.entrySet()) {
            if (entry.getValue() != null) {
                String longName = sanitizeChars(entry.getValue().getLongName());
                String shortName = sanitizeChars(entry.getValue().getShortName());

                entry.getValue().setLongName(longName);
                entry.getValue().setShortName(shortName);
            }
        }
    }

    private static String sanitizeChars(String name) {
        if (name == null) {
            return null;
        } else {
            return name.replaceAll("–", "-") // EN DASH, &#x2013
                    .replaceAll("Œ", "OE")
                    .replaceAll("œ", "oe")
                    .replaceAll("Ÿ", "Y")
                    .replaceAll("Ĳ", "IJ")
                    .replaceAll("ĳ", "ij")
                    .replaceAll("\"", "");
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
                        if (StringUtils.isNotBlank(component.getLongName())) {
                            return component.getLongName().trim();
                        }
                    } else {
                        if (StringUtils.isNotBlank(component.getShortName())) {
                            return component.getShortName().trim();
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
        administrative_area_level_1, // indicates a first-order civil entity below the country level. Within the United States, these administrative levels are states. Not all nations exhibit these administrative levels.
        administrative_area_level_2, // indicates a second-order civil entity below the country level. Within the United States, these administrative levels are counties. Not all nations exhibit these administrative levels.
        administrative_area_level_3, // indicates a third-order civil entity below the country level. This type indicates a minor civil division. Not all nations exhibit these administrative levels.
        administrative_area_level_4, // indicates a fourth-order civil entity below the country level. This type indicates a minor civil division. Not all nations exhibit these administrative levels.
        administrative_area_level_5, // indicates a fifth-order civil entity below the country level. This type indicates a minor civil division. Not all nations exhibit these administrative levels.
        colloquial_area, // indicates a commonly-used alternative name for the entity.
        country, // indicates the national political entity, and is typically the highest order type returned by the Geocoder.
        establishment, // typically indicates a place that has not yet been categorized.
        finance, //
        floor, // indicates the floor of a building address.
        food,
        general_contractor,
        geocode,
        health,
        intersection, //indicates a major intersection, usually of two major roads.
        locality, // indicates an incorporated city or town political entity.
        natural_feature, // indicates a prominent natural feature.
        neighborhood, //indicates a named neighborhood
        place_of_worship,
        political, // indicates a political entity. Usually, this type indicates a polygon of some civil administration.
        point_of_interest, // indicates a named point of interest. Typically, these "POI"s are prominent local entities that don't easily fit in another category such as "Empire State Building" or "Statue of Liberty."
        post_box, //indicates a specific postal box.
        postal_code, // indicates a postal code as used to address postal mail within the country.
        postal_code_prefix,
        postal_code_suffix,
        postal_town, //indicates a grouping of geographic areas, such as locality and sublocality, used for mailing addresses in some countries.
        premise, //indicates a named location, usually a building or collection of buildings with a common name
        room, // indicates the room of a building address.
        route, // indicates a named route (such as "US 101").
        street_address, // indicates a precise street address.
        street_number, //indicates the precise street number.
        sublocality, //indicates a first-order civil entity below a locality. For some locations may receive one of the additional types: sublocality_level_1 through to sublocality_level_5. Each sublocality level is a civil entity. Larger numbers indicate a smaller geographic area.
        sublocality_level_1, // see above
        sublocality_level_2, // see above
        sublocality_level_3, // see above
        sublocality_level_4, // see above
        sublocality_level_5, // see above
        subpremise, //indicates a first-order entity below a named location, usually a singular building within a collection of buildings with a common name

        // ADDITIONAL
        accounting,
        airport, // indicates an airport.
        amusement_park,
        aquarium,
        art_gallery,
        atm,
        bakery,
        bank,
        bar,
        beauty_salon,
        bicycle_store,
        book_store,
        bowling_alley,
        bus_station, // indicate the location of a bus, train or public transit stop.
        cafe,
        campground,
        car_dealer,
        car_rental,
        car_repair,
        car_wash,
        casino,
        cemetery,
        church,
        city_hall,
        clothing_store,
        convenience_store,
        courthouse,
        dentist,
        department_store,
        doctor,
        electrician,
        electronics_store,
        embassy,
        fire_station,
        florist,
        funeral_home,
        furniture_store,
        gas_station,
        @Deprecated grocery_or_supermarket,
        gym,
        hair_care,
        hardware_store,
        hindu_temple,
        home_goods_store,
        hospital,
        insurance_agency,
        jewelry_store,
        laundry,
        lawyer,
        library,
        liquor_store,
        local_government_office,
        locksmith,
        lodging,
        meal_delivery,
        meal_takeaway,
        mosque,
        movie_rental,
        movie_theater,
        moving_company,
        museum,
        night_club,
        painter,
        park, // indicates a named park.
        parking, //indicates a parking lot or parking structure.
        pet_store,
        pharmacy,
        physiotherapist,
        plumber,
        police,
        post_office,
        real_estate_agency,
        restaurant,
        roofing_contractor,
        rv_park,
        school,
        shoe_store,
        shopping_mall,
        spa,
        stadium,
        storage,
        store,
        subway_station,
        synagogue,
        taxi_stand,
        train_station,
        transit_station,
        travel_agency,
        university,
        veterinary_care,
        zoo
    }

    private enum GType {
        intersection
    }

    //

    public static void assignPlaceDetails(Address address, PlaceDetailsResult details) {
        String name = details.getName();
        AddressData data = address.getAddressData();
        if (!containsAny(details.getTypes(), GElement.street_address.name(), GElement.premise.name(), GElement.subpremise.name())
                && StringUtils.isNotBlank(name) && isCompanyName(address, name)) {
            name = name.replace(", ", " ")
                    .replace(",", " ");

            data.getAddressComponents().setCompany(name);
            data.setFormattedAddress(name + ", " + data.getFormattedAddress());
            data.getAddressComponents().setAddress(name + ", " + data.getAddressComponents().getAddress());
        } else if (containsAny(details.getTypes(), GElement.premise.name()) && StringUtils.isBlank(data.getAddressComponents().getBuildingName())) {
            name = name.replace(", ", " ")
                    .replace(",", " ");

            data.getAddressComponents().setBuildingName(name);
            data.setFormattedAddress(name + ", " + data.getFormattedAddress());
            data.getAddressComponents().setAddress(name + ", " + data.getAddressComponents().getAddress());
        }
    }

    @SafeVarargs
    private static <T> boolean containsAny(Collection<T> collection, T... elements) {
        for (T e : elements) {
            if (collection.contains(e))
                return true;
        }

        return false;
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
}
