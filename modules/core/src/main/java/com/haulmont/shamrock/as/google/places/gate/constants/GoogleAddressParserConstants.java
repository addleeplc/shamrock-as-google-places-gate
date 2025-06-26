/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.constants;

import com.haulmont.shamrock.as.dto.AddressType;
import com.haulmont.shamrock.as.google.places.gate.dto.enums.GElement;

import java.util.HashMap;
import java.util.Map;

public class GoogleAddressParserConstants {
    public static final Map<String, String> elementsSpecifics = new HashMap<>();
    public static final Map<String, AddressType> elementsTypes = new HashMap<>();

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
}
