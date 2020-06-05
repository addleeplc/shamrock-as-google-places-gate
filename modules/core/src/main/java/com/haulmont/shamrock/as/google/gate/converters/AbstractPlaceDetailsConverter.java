/*
 * Copyright 2008 - 2018 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.converters;

import com.haulmont.monaco.AppContext;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.dto.AddressType;
import com.haulmont.shamrock.as.google.gate.ServiceConfiguration;
import com.haulmont.shamrock.as.google.gate.constants.GoogleAddressParserConstants;
import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.PlaceDetails;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.dto.enums.GType;
import com.haulmont.shamrock.as.google.gate.utils.GoogleAddressUtils;
import com.haulmont.shamrock.as.utils.AddressBuilder;
import com.haulmont.shamrock.as.utils.AddressHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public abstract class AbstractPlaceDetailsConverter implements PlaceDetailsConverter {

    // Abstract methods

    public Address convert(PlaceDetails place, Map<String, AddressComponent> components) {
        String placeName = place.getName();
        String formattedAddress = StringUtils.isBlank(place.getFormattedAddress()) ? place.getVicinity() : place.getFormattedAddress();

        List<String> types = place.getTypes();

        if (isAirport(types))
            throw new RuntimeException("Address is airport");

        String country = getCountry(components);
        if (StringUtils.isBlank(country))
            throw new RuntimeException("Country is null");

        // prepare address components
        prepareComponents(components);

        String city = parseCity(components);
        if (StringUtils.isBlank(city))
            throw new RuntimeException("City is null");

        String postcode = parsePostcode(components);
        if (StringUtils.isBlank(postcode) && isCountryRequiredPostcode(country))
            throw new RuntimeException("Postcode is null");

        String companyName = parseCompanyName(placeName, components, types);
        String buildingName = parseBuildingName(placeName, components, types);

        String subBuildingName = parseSubBuildingName(placeName, components, types);
        String subBuildingNumber = parseSubBuildingNumber(formattedAddress, components);

        String buildingNumber = parseBuildingNumber(formattedAddress, components);
        String street = parseStreet(formattedAddress, components);
        if (StringUtils.isNotBlank(street) && StringUtils.equalsAny(street, AddressHelper.ParseStreetNameConstants.STREET_SUFFIXES.split("\\|")))
            street = null;

        ParseAddressContext ctx = new ParseAddressContext();
        ctx.company = companyName;

        if (StringUtils.isBlank(subBuildingNumber) && StringUtils.isNotBlank(buildingName) && buildingName.matches("[0-9]+[A-Za-z]?")) {
            ctx.subBuildingNumber = buildingName;
            subBuildingNumber = buildingName;
            ctx.building = null;
            buildingName = null;
        } else {
            ctx.subBuildingNumber = subBuildingNumber;
            ctx.building = buildingName;
        }

        ctx.buildingNumber = buildingNumber;
        ctx.street = street;
        ctx.city = city;

        if (street == null && StringUtils.isNotBlank(formattedAddress)) {
            if (formattedAddress.contains(ctx.city)) {
                street = formattedAddress.substring(0, formattedAddress.indexOf(ctx.city));

                if (StringUtils.contains(street, ctx.company)) street = street.replace(ctx.company, "");
                if (StringUtils.contains(street, ctx.building)) street = street.replace(ctx.building, "");

                if (StringUtils.isNotBlank(street)) {
                    street = street.replace(", ", " ").replaceAll("\\s+", " ").trim();
                    ctx.street = street;
                }
            }
        }

        com.haulmont.shamrock.as.dto.Location location = GoogleAddressUtils.convert(place.getGeometry());

        String specifics = parseAddressSpecifics(types);
        AddressType addressType = parseAddressType(types);

        Address a = new AddressBuilder(city, country)
                .postcode(postcode)
                .company(companyName)
                .buildingName(buildingName)
                .buildingNumber(buildingNumber)
                .subBuildingName(subBuildingName)
                .subBuildingNumber(subBuildingNumber)
                .street(street)
                .location(location)
                .specifics(specifics)
                .addressType(addressType)
                .build();
        String address = parseAddress(formattedAddress, components, types, ctx);
        if (StringUtils.isBlank(address)) address = AddressHelper.buildAddress(a);

        if (StringUtils.isBlank(address)) throw new RuntimeException("Address is null");
        if (address.endsWith(", ")) address = address.substring(0, address.length() - 2);

        a.getAddressData().getAddressComponents().setAddress(address);
        a.getAddressData().setFormattedAddress(AddressHelper.getName(a));

        return a;
    }

    protected abstract void prepareComponents(Map<String, AddressComponent> components);

    protected abstract String parseCity(Map<String, AddressComponent> components);

    protected abstract String parsePostcode(Map<String, AddressComponent> components);

    protected abstract String parseBuildingName(String placeName, Map<String, AddressComponent> components, List<String> types);

    protected abstract String parseSubBuildingName(String placeName, Map<String, AddressComponent> components, List<String> types);

    protected abstract String parseCompanyName(String placeName, Map<String, AddressComponent> components, List<String> types);

    protected abstract String parseBuildingNumber(String formattedAddress, Map<String, AddressComponent> components);

    protected abstract String parseSubBuildingNumber(String formattedAddress, Map<String, AddressComponent> components);

    protected abstract String parseStreet(String formattedAddress, Map<String, AddressComponent> components);

    protected abstract String parseAddress(String formattedAddress, Map<String, AddressComponent> components, List<String> types, ParseAddressContext ctx);

    protected String getCountry(Map<String, AddressComponent> components) {
        return GoogleAddressUtils.getFirstShort(components, GElement.country, GElement.political);
    }

    protected String parseAddressSpecifics(Collection<String> types) {
        Optional<String> o = types.stream()
                .filter(GoogleAddressParserConstants.elementsSpecifics::containsKey)
                .map(GoogleAddressParserConstants.elementsSpecifics::get)
                .findFirst();

        return o.orElse(null);
    }

    protected AddressType parseAddressType(Collection<String> types) {
        Optional<AddressType> o = types.stream()
                .filter(GoogleAddressParserConstants.elementsTypes::containsKey)
                .map(GoogleAddressParserConstants.elementsTypes::get)
                .findFirst();

        return o.orElse(null);
    }

    // Util methods

    protected boolean isAirport(Collection<String> types) {
        Optional<String> o = types.stream()
                .filter(t -> t.equals(GElement.airport.name()))
                .findFirst();

        return o.isPresent();
    }

    protected boolean isCompanyName(String address, String placeName) {
        if (StringUtils.containsIgnoreCase(address, placeName)) return false;

        String[] nComponents = placeName.split(" ");
        int idx = address.indexOf(",");
        String[] aComponents = (idx < 0 ? address : address.substring(0, idx)).split(" ");

        if (nComponents.length != aComponents.length) return true;
        int i = 0;
        for (String aComponent : aComponents) {
            if (StringUtils.startsWith(aComponent, nComponents[i])) return false;
            i++;
        }

        return true;
    }

    protected boolean isCountryRequiredPostcode(String country) {
        ServiceConfiguration conf = AppContext.getConfig().get(ServiceConfiguration.class);

        String s = conf.getCountriesRequirePostcode();
        if (StringUtils.isBlank(s)) return false;

        s = StringUtils.deleteWhitespace(s);
        return (new HashSet<>(Arrays.asList(s.split("[;,]"))).contains(country));
    }

    protected String getFirstLong(Map<String, AddressComponent> components, GElement... elements) {
        return getFirst(components, true, elements);
    }

    protected String getFirstShort(Map<String, AddressComponent> components, GElement... elements) {
        return getFirst(components, false, elements);
    }

    protected String getExact(Map<String, AddressComponent> components, boolean shortName, GElement... elements) {
        if (elements == null || elements.length == 0) {
            return null;
        } else {
            Set<String> sElems = new HashSet<>();
            for (GElement element : elements) {
                sElems.add(element.name());
            }

            for (AddressComponent addressComponent : components.values()) {
                if (addressComponent.getTypes().containsAll(sElems))
                    return StringUtils.trim(shortName ? addressComponent.getShortName() : addressComponent.getLongName());
            }

            return null;
        }
    }

    private String getFirst(Map<String, AddressComponent> components, boolean isGetLongName, GElement... elements) {

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

    protected boolean isType(List<String> types, GType value) {
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

    protected void sanitizeAddress(Map<String, AddressComponent> components) {

        for (Map.Entry<String, AddressComponent> entry : components.entrySet()) {
            if (entry.getValue() != null) {
                String longName = sanitizeChars(entry.getValue().getLongName());
                String shortName = sanitizeChars(entry.getValue().getShortName());

                entry.getValue().setLongName(longName);
                entry.getValue().setShortName(shortName);
            }
        }
    }

    private String sanitizeChars(String name) {
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

    protected void transliterateComponents(Map<String, AddressComponent> components, Map<String, String> transliterationTable) {
        for (Map.Entry<String, AddressComponent> entry : components.entrySet()) {
            if (entry.getValue() != null) {
                String longName = transliterate(entry.getValue().getLongName(), transliterationTable);
                String shortName = transliterate(entry.getValue().getShortName(), transliterationTable);

                entry.getValue().setLongName(longName);
                entry.getValue().setShortName(shortName);
            }
        }
    }

    protected String transliterate(String origin, Map<String, String> transliterationTable) {
        if (StringUtils.isBlank(origin))
            return origin;

        boolean b = false;
        for (String s : transliterationTable.keySet()) {
            if (origin.contains(s)) {
                b = true;
                break;
            }
        }

        if (b) {
            char[] chars = origin.toCharArray();
            StringBuilder sb = new StringBuilder();
            for (char ch : chars) {
                if (transliterationTable.containsKey(String.valueOf(ch)))
                    sb.append(transliterationTable.get(String.valueOf(ch)));
                else
                    sb.append(ch);
            }

            return sb.toString();
        } else {
            return origin;
        }
    }

    protected static class ParseAddressContext {
        public String company;
        public String building;
        public String buildingNumber;
        public String subBuildingNumber;
        public String street;
        public String city;
    }
}
