/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.cache;

import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.config.annotations.Config;
import com.haulmont.monaco.config.annotations.Property;
import com.haulmont.monaco.jmx.MBeanUtils;
import com.haulmont.monaco.model.cache.CacheManagement;
import com.haulmont.monaco.redis.Redis;
import com.haulmont.monaco.redis.cache.RedisCache;
import com.haulmont.monaco.redis.cache.RedisCacheKeyCodec;
import com.haulmont.monaco.redis.cache.RedisCacheObjectCodec;
import com.haulmont.monaco.redis.cache.codec.JacksonObjectListCodec;
import com.haulmont.monaco.redis.cache.codec.PropertyObjectCodec;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.google.places.gate.cache.dto.GeneralSearchContext;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.Geometry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.hash.Hashing;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class GeocodeCache implements CacheManagement {

    public static final int DEFAULT_EXPIRATION_MINUTES = 60;

    @Inject
    private CacheConfiguration configuration;

    //

    private final RedisCacheKeyCodec<GeneralSearchContext> keyCodec = new SearchContextCodec("address-search", "places-cache");
    private final RedisCacheObjectCodec<List<Address>> valueCodec = new JacksonObjectListCodec<>(Address.class);

    private RedisCache<GeneralSearchContext, List<Address>> cache;

    //

    @SuppressWarnings("unused")
    public void start() {
        cache = RedisCache.<GeneralSearchContext, List<Address>>builder()
                .setRedis(getRedis())
                .setKeyCodec(keyCodec)
                .setValueCodec(valueCodec)
                .expireAfterWrite(getExpirationMinutes(), TimeUnit.MINUTES)
                .build();

        MBeanUtils.register(this, CacheManagement.class, getClass().getPackage().getName() + ":type=" + getClass().getSimpleName());
    }

    //

    public List<Address> get(GeneralSearchContext search) {
        return cache.getIfPresent(search);
    }

    public <T> List<Address> getOrLookup(T context, Function<T, GeneralSearchContext> converterToGeneralSearchContext, Function<T, List<Address>> lookupFunction) {
        GeneralSearchContext generalSearchContext = converterToGeneralSearchContext.apply(context);
        List<Address> addresses = cache.getIfPresent(generalSearchContext);
        if (addresses == null)
            addresses = lookupFunction.apply(context);

        if (!CollectionUtils.isEmpty(addresses))
            cache.put(generalSearchContext, addresses);

        return addresses;
    }

    public <T> Address getOrLookupOne(T context, Function<T, GeneralSearchContext> converter, Function<T, Address> lookupFunction) {
        GeneralSearchContext generalSearchContext = converter.apply(context);
        List<Address> addresses = cache.getIfPresent(generalSearchContext);
        final Address address;
        if (addresses == null) {
            address = lookupFunction.apply(context);
            addresses = address != null ? List.of(address) : List.of();
        } else
            address = addresses.isEmpty() ? null : addresses.get(0);

        if (!CollectionUtils.isEmpty(addresses))
            cache.put(generalSearchContext, addresses);

        return address;
    }

    public void put(GeneralSearchContext search, List<Address> addresses) {
        cache.put(search, addresses);
    }

    //

    @Override
    public long getSize() {
        return cache.size();
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    @SuppressWarnings("unchecked")
    protected Redis<String, String> getRedis() {
        return AppContext.getResources().get(getRedisResource(), Redis.class);
    }

    private String getRedisResource() {
        return Optional.ofNullable(configuration.getRedisResource()).orElse("redis-cache");
    }

    private Integer getExpirationMinutes() {
        return Optional.ofNullable(configuration.getExpirationMinutes()).orElse(DEFAULT_EXPIRATION_MINUTES);
    }

    //

    @Config
    @Component
    public interface CacheConfiguration {
        @Property("caches.expirationMinutes")
        Integer getExpirationMinutes();

        @Property("caches.redis.resourceName")
        String getRedisResource();
    }


    public static class SearchContextCodec extends PropertyObjectCodec<GeneralSearchContext> {

        public SearchContextCodec(String group, String name) {
            super(group, GeneralSearchContext.class, name);
        }

        @Override
        public String encodeValue(GeneralSearchContext o) {
            StringBuilder key = new StringBuilder();

            add(key, "OPR", o.getOperationName());
            add(key, "S", o.getSearchString());
            add(key, "A", o.getAddress());
            add(key, "CT", o.getCity());
            add(key, "CN", o.getCountry());
            add(key, "P", o.getPlaceId());
            add(key, "PC", o.getPostcode());
            add(key, "pCT", o.getPreferredCity());
            add(key, "pCN", o.getPreferredCountry());
            add(key, "LA", encodeGeometry(o.getLocationBias()));
            add(key, "OG", o.getLocation() != null ? Converters.asString(o.getLocation().getLatitude())+'x'+Converters.asString(o.getLocation().getLatitude()) : null);
            add(key, "PG", o.getPreferGeocoding() != null ? o.getPreferGeocoding().toString() : null);

            return Hashing.sha256().hashString(key.toString(), StandardCharsets.UTF_8).toString();
        }

        private String encodeGeometry(Geometry g) {
            if(g==null || (g.getCircle() == null && g.getRectangle() == null))
                return null;
            StringBuilder builder = new StringBuilder();
            if( g.getCircle() != null) {
                add(builder, "Cx", Converters.asString(g.getCircle().getCenter().getLatitude()));
                add(builder, "Cy", Converters.asString(g.getCircle().getCenter().getLongitude()));
                add(builder, "R", Converters.asString(g.getCircle().getRadius()));
            } else if (g.getRectangle() != null && g.getRectangle().getHigh() != null && g.getRectangle().getLow() != null) {
                add(builder, "x1", Converters.asString(g.getRectangle().getHigh().getLatitude()));
                add(builder, "y1", Converters.asString(g.getRectangle().getHigh().getLongitude()));
                add(builder, "x2", Converters.asString(g.getRectangle().getLow().getLatitude()));
                add(builder, "y2", Converters.asString(g.getRectangle().getLow().getLongitude()));
            }
            return builder.toString();
        }

        private void add(StringBuilder builder, String code, String value) {
            if (StringUtils.isNotEmpty(value))
                builder.append(code).append("(").append(value).append(")");
        }

        @Override
        public GeneralSearchContext decodeValue(String s) {
            throw new UnsupportedOperationException();
        }
    }
}