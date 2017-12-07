/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.parser;

import com.haulmont.monaco.AppContext;
import com.haulmont.shamrock.address.*;
import com.haulmont.shamrock.address.utils.AddressHelper;
import com.haulmont.shamrock.as.google.gate.GateConfiguration;
import com.haulmont.shamrock.as.google.gate.constants.GoogleAddressParserConstants;
import com.haulmont.shamrock.as.google.gate.dto.AddressComponent;
import com.haulmont.shamrock.as.google.gate.dto.Geometry;
import com.haulmont.shamrock.as.google.gate.dto.Location;
import com.haulmont.shamrock.as.google.gate.dto.enums.GElement;
import com.haulmont.shamrock.as.google.gate.dto.enums.GType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public abstract class AbstractGoogleAddressParser {
    private final String country;

    protected AbstractGoogleAddressParser() {
        this.country = getClass().getAnnotation(Parser.class).value();
    }

    // Abstract methods

    public Address parse(String placeName, String formattedAddress, Geometry geometry, Map<String, AddressComponent> components, List<String> types) throws AddressParseException {
        if (isAirport(types))
            throw new AddressParseException("Address is airport");

        String country = getCountry(components);
        if (StringUtils.isBlank(country))
            throw new AddressParseException("Country is null");

        // prepare address components
        prepareComponents(components);

        String city = parseCity(components);
        if (StringUtils.isBlank(city))
            throw new AddressParseException("City is null");

        String postcode = parsePostcode(components);
        if (StringUtils.isBlank(postcode) && isCountryRequiredPostcode(country))
            throw new AddressParseException("Postcode is null");

        String companyName = parseCompanyName(placeName, components, types);
        String buildingName = parseBuildingName(placeName, components, types);

        String buildingNumber = parseBuildingNumber(components);
        String street = parseStreet(formattedAddress, components);
        if (StringUtils.isNotBlank(street) && StringUtils.equalsAny(street, AddressHelper.ParseStreetNameConstants.STREET_SUFFIXES.split("\\|")))
            street = null;

        ParseAddressContext ctx = new ParseAddressContext();
        ctx.company = companyName;

        if (buildingName.matches("[0-9]+[A-Za-z]?"))
            ctx.subBuildingNumber = buildingName;
        else
            ctx.building = buildingName;

        ctx.buildingNumber = buildingNumber;
        ctx.street = street;
        ctx.city = city;

        String address = parseAddress(formattedAddress, components, types, ctx);
        if (StringUtils.isBlank(address)) {
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNotBlank(companyName))
                sb.append(companyName).append(',').append(' ');

            if (StringUtils.isNotBlank(buildingName))
                sb.append(buildingName).append(',').append(' ');

            if (StringUtils.isNotBlank(buildingNumber) && StringUtils.isNotBlank(street))
                sb.append(buildingNumber).append(' ');

            if (StringUtils.isNotBlank(street))
                sb.append(street);

            address = sb.toString();

            if (StringUtils.endsWith(address, ", ") || StringUtils.endsWith(address, ","))
                address = address.substring(0, address.lastIndexOf(","));
        }

        if (StringUtils.isBlank(address))
            throw new AddressParseException("Address is null");

        com.haulmont.shamrock.address.Location location = parseLocation(geometry);

        String specifics = parseAddressSpecifics(types);
        AddressType addressType = parseAddressType(types);

        return new AddressBuilder(city, country)
                .postcode(postcode)
                .company(companyName)
                .buildingName(buildingName)
                .buildingNumber(buildingNumber)
                .street(street)
                .address(address)
                .location(location)
                .specifics(specifics)
                .addressType(addressType)
                .build();
    }

    protected abstract void prepareComponents(Map<String, AddressComponent> components);

    protected abstract String parseCity(Map<String, AddressComponent> components);

    protected abstract String parsePostcode(Map<String, AddressComponent> components);

    protected abstract String parseBuildingName(String placeName, Map<String, AddressComponent> components, List<String> types);

    protected abstract String parseCompanyName(String placeName, Map<String, AddressComponent> components, List<String> types);

    protected abstract String parseBuildingNumber(Map<String, AddressComponent> components);

    protected abstract String parseStreet(String formattedAddress, Map<String, AddressComponent> components);

    protected abstract String parseAddress(String formattedAddress, Map<String, AddressComponent> components, List<String> types, ParseAddressContext ctx);

    protected String getCountry(Map<String, AddressComponent> components) {
        return getClass().getAnnotation(Parser.class).value();
    }

    protected com.haulmont.shamrock.address.Location parseLocation(Geometry geometry) {
        if (geometry != null) {
            Location location = geometry.getLocation();
            if (location != null) {
                com.haulmont.shamrock.address.Location l = new com.haulmont.shamrock.address.Location();
                l.setLat(location.getLat());
                l.setLon(location.getLng());

                return l;
            }
        }

        return null;
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
        GateConfiguration conf = AppContext.getConfig().get(GateConfiguration.class);

        String s = conf.getCountriesNotRequiredPostcode();
        if (StringUtils.isBlank(s))
            return true;

        s = StringUtils.deleteWhitespace(s);
        return !(new HashSet<>(Arrays.asList(s.split("[;,]"))).contains(country));
    }

    protected String getFirstLong(Map<String, AddressComponent> components, GElement... elements) {
        return getFirst(components, true, elements);
    }

    protected String getFirstShort(Map<String, AddressComponent> components, GElement... elements) {
        return getFirst(components, false, elements);
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

    protected static class AddressBuilder {
        private String city;
        private String country;

        //

        private String address;
        private String company;
        private String buildingName;
        private String subBuildingName;
        private String buildingNumber;
        private String subBuildingNumber;
        private String street;
        private String postcode;

        //

        private com.haulmont.shamrock.address.Location location;

        private String notes;

        //

        @Deprecated
        private String specifics;
        private AddressType addressType;

        public AddressBuilder(String city, String country) {
            this.city = city;
            this.country = country;
        }

        public AddressBuilder address(String address) {
            this.address = address;
            return this;
        }

        public AddressBuilder company(String company) {
            this.company = company;
            return this;
        }

        public AddressBuilder buildingName(String buildingName) {
            this.buildingName = buildingName;
            return this;
        }

        public AddressBuilder subBuildingName(String subBuildingName) {
            this.subBuildingName = subBuildingName;
            return this;
        }

        public AddressBuilder buildingNumber(String buildingNumber) {
            this.buildingNumber = buildingNumber;
            return this;
        }

        public AddressBuilder subBuildingNumber(String subBuildingNumber) {
            this.subBuildingNumber = subBuildingNumber;
            return this;
        }

        public AddressBuilder street(String street) {
            this.street = street;
            return this;
        }

        public AddressBuilder postcode(String postcode) {
            this.postcode = postcode;
            return this;
        }

        public AddressBuilder location(com.haulmont.shamrock.address.Location location) {
            this.location = location;
            return this;
        }

        public AddressBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        @Deprecated
        public AddressBuilder specifics(String specifics) {
            this.specifics = specifics;
            return this;
        }

        public AddressBuilder addressType(AddressType addressType) {
            this.addressType = addressType;
            return this;
        }

        public Address build() {
            AddressComponents components = buildAddressComponents();

            AddressData data = new AddressData();
            data.setFormattedAddress(buildFormattedAddress());
            data.setAddressComponents(components);
            data.setLocation(location);
            data.setNotes(notes);

            AddressDetails details = new AddressDetails();
            details.setSpecifics(specifics);
            details.setType(addressType);

            Address address = new Address();
            address.setType(ItemType.ADDRESS);
            address.setAddressData(data);
            address.setDetails(details);

            return address;
        }

        private AddressComponents buildAddressComponents() {
            AddressComponents components = new AddressComponents();
            components.setAddress(address);

            components.setBuildingName(buildingName);
            components.setSubBuildingName(subBuildingName);

            components.setCompany(company);

            components.setBuildingNumber(buildingNumber);
            components.setSubBuildingNumber(subBuildingNumber);

            components.setStreet(street);
            components.setPostcode(postcode);

            components.setCity(city);
            components.setCountry(country);
            return components;
        }

        private String buildFormattedAddress() {
            String formattedAddress = address;
            if (!StringUtils.containsIgnoreCase(address, ", " + city))
                formattedAddress = formattedAddress + ", " + city;

            if (StringUtils.isNotBlank(postcode) && !StringUtils.containsIgnoreCase(address, ", " + postcode))
                formattedAddress = formattedAddress + ", " + postcode;

            if (StringUtils.isNotBlank(buildingName) && !StringUtils.containsIgnoreCase(formattedAddress, buildingName + ", "))
                formattedAddress = buildingName + ", " + formattedAddress;

            if (StringUtils.isNotBlank(company) && !StringUtils.containsIgnoreCase(formattedAddress, company + ", "))
                formattedAddress = company + ", " + formattedAddress;

            return formattedAddress;
        }
    }
}
