/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parsers;

import com.haulmont.bali.lang.MapUtils;
import com.haulmont.shamrock.as.dto.AddressComponents;
import com.haulmont.shamrock.as.google.gate.dto.Place;
import com.haulmont.shamrock.geo.utils.PostalCodeUtils;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;

import java.util.List;

@Component
@PlaceParser.Component({PlaceParser_IT.COUNTRY_NAME})
public class PlaceParser_IT extends AbstractPlaceParser {

    public static final String ISO_COUNTRY_CODE = "IT";

    //

    public static final String COUNTRY_NAME = "Italy";

    private static final String COUNTRY_NAME_SUFFIX = COMPONENTS_DIVIDER + COUNTRY_NAME;

    //

    public AddressComponents parse(Place place) {
        String formattedAddress = place.getFormattedAddress();

        String suffix = COUNTRY_NAME_SUFFIX;
        if (formattedAddress.endsWith(suffix)) {
            return __parse(place, suffix);
        } else {
            return null;
        }
    }

    private AddressComponents __parse(Place place, String suffix) {
        String formattedAddress = place.getFormattedAddress();

        AddressComponents components = new AddressComponents();
        components.setCountry(ISO_COUNTRY_CODE);

        String s = getSubstring(formattedAddress, suffix);

        String[] parts = s.split(COMPONENTS_DIVIDER);
        String part = parts[parts.length - 1];

        String postcode = PostalCodeUtils.IT.parse(part);
        if (postcode != null) {
            if (part.startsWith(postcode)) {
                components.setPostcode(postcode);

                parts[parts.length - 1] = part.substring(postcode.length()).trim();

                return parseAddressComponents(place, parts, components);
            } else {
                return null;
            }
        } else {
            return parseAddressComponents(place, parts, components);
        }
    }

    private AddressComponents parseAddressComponents(Place place, String[] parts, AddressComponents components) {
        if (parts.length < 2) return null;

        String part = parts[parts.length - 1].trim();

        String province;

        String prefix = "Province of ";
        if (part.startsWith(prefix)) {
            String t = part.substring(prefix.length());
            if (PROVINCES.inverseBidiMap().containsKey(t.trim())) {
                province = t.trim();
            } else {
                province = null;
            }

            String city = parts[parts.length - 2].trim();

            components.setCity(city);
            components.setAddress(getAddress(place, concat(parts, parts.length - 2)));
        } else {
            String t = part.substring(part.length() - 3);
            if (t.startsWith(" ") && PROVINCES.containsKey(t.trim())) {
                province = t.trim();
            } else {
                province = null;
            }

            String city;

            if (StringUtils.isNotBlank(province)) {
                city = part.substring(0, part.length() - province.length()).trim();
            } else {
                city = part;
            }

            components.setCity(city);
            components.setAddress(getAddress(place, concat(parts, parts.length - 1)));
        }

        if (isBusinessName(place)) {
            components.setCompany(place.getName());
        }

        return components;
    }

    //

    private static final BidiMap<String, String> PROVINCES = MapUtils.bidiMapOf(
            "AG","Agrigento",
            "AL","Alessandria",
            "AN","Ancona",
            "AO","Aosta",
            "AR","Arezzo",
            "AP","Ascoli Piceno",
            "AT","Asti",
            "AV","Avellino",
            "BA","Bari",
            "BT","Barletta-Andria-Trani",
            "BL","Belluno",
            "BN","Benevento",
            "BG","Bergamo",
            "BI","Biella",
            "BO","Bologna",
            "BS","Brescia",
            "BR","Brindisi",
            "CA","Cagliari",
            "CL","Caltanissetta",
            "CB","Campobasso",
            "CE","Caserta",
            "CT","Catania",
            "CZ","Catanzaro",
            "CH","Chieti",
            "CO","Como",
            "CS","Cosenza",
            "CR","Cremona",
            "KR","Crotone",
            "CN","Cuneo",
            "EN","Enna",
            "FM","Fermo",
            "FE","Ferrara",
            "FI","Florence",
            "FG","Foggia",
            "FC","Forlì-Cesena",
            "FR","Frosinone",
            "GE","Genoa",
            "GO","Gorizia",
            "GR","Grosseto",
            "IM","Imperia",
            "IS","Isernia",
            "SP","La Spezia",
            "AQ","L'Aquila",
            "LT","Latina",
            "LE","Lecce",
            "LC","Lecco",
            "LI","Livorno",
            "LO","Lodi",
            "LU","Lucca",
            "MC","Macerata",
            "MN","Mantua",
            "MS","Massa and Carrara",
            "MT","Matera",
            "ME","Messina",
            "MI","Milan",
            "MO","Modena",
            "MB","Monza and Brianza",
            "NA","Naples",
            "NO","Novara",
            "NU","Nuoro",
            "OR","Oristano",
            "PD","Padua",
            "PA","Palermo",
            "PR","Parma",
            "PV","Pavia",
            "PG","Perugia",
            "PU","Pesaro and Urbino",
            "PE","Pescara",
            "PC","Piacenza",
            "PI","Pisa",
            "PT","Pistoia",
            "PN","Pordenone",
            "PZ","Potenza",
            "PO","Prato",
            "RG","Ragusa",
            "RA","Ravenna",
            "RC","Reggio Calabria",
            "RE","Reggio Emilia",
            "RI","Rieti",
            "RN","Rimini",
            "RM","Rome",
            "RO","Rovigo",
            "SA","Salerno",
            "SS","Sassari",
            "SV","Savona",
            "SI","Siena",
            "SO","Sondrio",
            "SU","South Sardinia",
            "BZ","South Tyrol",
            "SR","Syracuse",
            "TA","Taranto",
            "TE","Teramo",
            "TR","Terni",
            "TP","Trapani",
            "TN","Trento",
            "TV","Treviso",
            "TS","Trieste",
            "TO","Turin",
            "UD","Udine",
            "VA","Varese",
            "VE","Venice",
            "VB","Verbano-Cusio-Ossola",
            "VC","Vercelli",
            "VR","Verona",
            "VV","Vibo Valentia",
            "VI","Vicenza",
            "VT","Viterbo"
    );
}
