/*
 * Copyright 2008 - 2025 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.places.gate.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.haulmont.monaco.PropertyEvent;
import com.haulmont.monaco.PropertyEventListener;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.annotations.Component;
import org.picocontainer.annotations.Inject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GoogleConfigurationService implements PropertyEventListener {

    private static final Pattern WILDCARD_PATTERN = Pattern.compile("[^*]+|(\\*)");
    private static final String WILDCARD_SIGN = "*";

    private static final String CHANNELS_MAPPING_PROPERTY_KEY_PREFIX = "mapping.channel.";
    private final Cache<String, String> channelsMapping = CacheBuilder.newBuilder().build();
    @Inject
    private GoogleConfigurationStorage configurationStorage;

    public void start() {
        Map<String, String> properties = getProperties();

        for (Map.Entry<String, String> e : properties.entrySet()) {
            String key = sanitizeChannelsMappingKey(e.getKey());
            if (StringUtils.isNotBlank(key)) {
                String value = e.getValue();
                if (StringUtils.isNotBlank(value)) channelsMapping.put(key, value);
            }
        }

        configurationStorage.registerListener("", this);
    }

    protected Map<String, String> getProperties() {
        return configurationStorage.getProperties("").getProperties();
    }

    @Override
    public void onEvent(PropertyEvent propertyEvent) {
        String key = propertyEvent.getKey();
        if (StringUtils.startsWith(key, CHANNELS_MAPPING_PROPERTY_KEY_PREFIX)) {
            key = sanitizeChannelsMappingKey(propertyEvent.getKey());
            if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(propertyEvent.getValue())) {
                switch (propertyEvent.getType()) {
                    case ADD:
                    case UPDATE:
                        this.channelsMapping.put(key, propertyEvent.getValue());
                        break;
                    case REMOVE:
                        this.channelsMapping.invalidate(key);
                }
            }
        }
    }

    private String sanitizeChannelsMappingKey(String key) {
        return StringUtils.replace(key, CHANNELS_MAPPING_PROPERTY_KEY_PREFIX, "");
    }

    public String getGoogleChannel(String channelId) {
        String googleChannel = channelsMapping.getIfPresent(channelId);
        if (StringUtils.isBlank(googleChannel)) {
            for (String key : channelsMapping.asMap().keySet()) {
                if (key.contains(WILDCARD_SIGN) && checkIfMatches(key, channelId))
                    return channelsMapping.getIfPresent(key);
            }
        }

        return googleChannel;
    }

    private boolean checkIfMatches(String key, String channelId) {
        Matcher m = WILDCARD_PATTERN.matcher(key);
        StringBuilder modifiedKey = new StringBuilder();
        while (m.find()) {
            if (m.group(1) != null) m.appendReplacement(modifiedKey, ".*");
            else m.appendReplacement(modifiedKey, "\\\\Q" + m.group(0) + "\\\\E");
        }
        m.appendTail(modifiedKey);

        return channelId.matches(modifiedKey.toString());
    }
}
