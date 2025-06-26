/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.dto.enums;

public enum GElement {
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
