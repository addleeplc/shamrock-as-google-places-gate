/*
 * Copyright 2008 - 2021 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate;

import com.haulmont.monaco.App;
import com.haulmont.monaco.AppContext;
import com.haulmont.monaco.TestAppContextLoader;
import com.haulmont.shamrock.as.google.gate.config.GoogleConfigurationStorage;
import com.haulmont.shamrock.as.google.gate.config.MockGoogleConfigurationService;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class ChannelMappingConfigurationTest extends TestAppContextLoader {
    private GoogleConfigurationStorage googleConfigurationStorage;
    private MockGoogleConfigurationService googleConfigurationService;

    @Test
    public void testChannelsMapping() {
        String tbsChannelId = googleConfigurationService.getGoogleChannel("tbs");
        Assert.assertEquals(tbsChannelId, "101");

        String csChannelId = googleConfigurationService.getGoogleChannel("cs");
        Assert.assertEquals(csChannelId, "102");

        String sherlockXChannelId = googleConfigurationService.getGoogleChannel("sherlock_X_integration");
        Assert.assertEquals(sherlockXChannelId, "999");

        String sherlockXpartnerChannelId = googleConfigurationService.getGoogleChannel("sherlock_X_partner");
        Assert.assertNull(sherlockXpartnerChannelId);

        String sherlockIntegrationChannelId = googleConfigurationService.getGoogleChannel("sherlock_integration");
        Assert.assertNull(sherlockIntegrationChannelId);
    }

    //

    @BeforeClass
    public void setUp() throws Exception {
        this.start();

        this.googleConfigurationStorage = AppContext.getBean(GoogleConfigurationStorage.class);

        this.googleConfigurationService = AppContext.getBean(MockGoogleConfigurationService.class);
        this.googleConfigurationService.start();
    }

    @AfterClass
    public void tearDown() throws IOException {
        this.stop();
    }
}
