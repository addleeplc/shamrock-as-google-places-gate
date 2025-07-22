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
import com.haulmont.monaco.redis.cache.RedisLoadingCache;
import com.haulmont.monaco.redis.cache.codec.JacksonObjectListCodec;
import com.haulmont.monaco.redis.cache.codec.PropertyObjectCodec;
import com.haulmont.shamrock.as.dto.Address;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.Circle;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.Geometry;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.LatLng;
import com.haulmont.shamrock.as.google.places.gate.services.dto.google.places.Viewport;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.hash.Hashing;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class SearchResCache implements CacheManagement {

    public static final int DEFAULT_EXPIRATION_MINUTES = 10;

    @Inject
    private CacheConfiguration configuration;

    //

    private final RedisCacheKeyCodec<SearchResKey> keyCodec = new SearchContextCodec("operations", "res");
    private final RedisCacheObjectCodec<List<Address>> valueCodec = new JacksonObjectListCodec<>(Address.class);

    private RedisCache<SearchResKey, List<Address>> cache;

    //

    @SuppressWarnings("unused")
    public void start() {
        cache = RedisLoadingCache.<SearchResKey, List<Address>>builder()
                .setRedis(getRedis())
                .setKeyCodec(keyCodec)
                .setValueCodec(valueCodec)
                .expireAfterWrite(getExpirationMinutes(), TimeUnit.MINUTES)
                .build();

        MBeanUtils.register(this, CacheManagement.class, getClass().getPackage().getName() + ":type=" + getClass().getSimpleName());
    }

    //

    public List<Address> get(SearchResKey key) {
        return cache.getIfPresent(key);
    }

    public List<Address> get(Supplier<SearchResKey> keySupplier) {
        return cache.getIfPresent(keySupplier.get());
    }

    public <C> List<Address> get(C context, SearchResKeyLoader<C> keyLoader) {
        SearchResKey key = keyLoader.getKey(context);

        try {
            return cache.get(key, () -> keyLoader.getValue(context));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    //

    public interface SearchResKeyLoader<T> {
        SearchResKey getKey(T context);
        List<Address> getValue(T context);
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


    public static class SearchContextCodec extends PropertyObjectCodec<SearchResKey> {

        public SearchContextCodec(String group, String name) {
            super(group, SearchResKey.class, name);
        }

        @Override
        public String encodeValue(SearchResKey o) {
            StringBuilder key = new StringBuilder();

            add(key, "S", o.getSearchString());
            add(key, "A", o.getAddress());
            add(key, "CT", o.getCity());
            add(key, "CN", o.getCountry());
            add(key, "P", o.getPlaceId());
            add(key, "PC", o.getPostcode());
            add(key, "pCT", o.getPreferredCity());
            add(key, "pCN", o.getPreferredCountry());
            add(key, "LA", encodeGeometry(o.getLocationBias()));
            add(key, "OG", o.getLocation() != null ? toString(o.getLocation().getLatitude()) + 'x' + toString(o.getLocation().getLatitude()) : null);

            return o.getOperationName() + ":" + Hashing.sha256().hashString(key.toString(), StandardCharsets.UTF_8);
        }

        private String encodeGeometry(Geometry g) {
            if (g == null) return null;

            Circle circle = g.getCircle();
            Viewport rectangle = g.getRectangle();

            if ((circle == null && rectangle == null)) return null;

            StringBuilder builder = new StringBuilder();
            if (circle != null) {
                LatLng center = circle.getCenter();
                add(builder, "cx", toString(center.getLatitude()));
                add(builder, "cy", toString(center.getLongitude()));
                add(builder, "r", toString(circle.getRadius()));
            } else {
                LatLng high = rectangle.getHigh();
                LatLng low = rectangle.getLow();
                if (high != null && low != null) {
                    add(builder, "x1", toString(high.getLatitude()));
                    add(builder, "y1", toString(high.getLongitude()));
                    add(builder, "x2", toString(low.getLatitude()));
                    add(builder, "y2", toString(low.getLongitude()));
                }
            }
            return builder.toString();
        }

        private static String toString(Double d) {
            return SearchResKeyBuilder.toString(d);
        }

        private <T> void add(StringBuilder builder, String code, String value) {
            if (StringUtils.isNotEmpty(value)) {
                builder.append(code).append("(").append(value).append(")");
            }
        }

        @Override
        public SearchResKey decodeValue(String s) {
            throw new UnsupportedOperationException();
        }
    }
}