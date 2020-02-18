/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.cp.nbasetcp;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * @author Taejin Koo
 */
public class NbasetCpConfigTest {

    @Test
    public void configTest1() throws Exception {
        NbasetCpConfig config = createConfig("false");

        Assert.assertFalse(config.isPluginEnable());
    }

    @Test
    public void configTest2() throws Exception {
        NbasetCpConfig config = createConfig("true");

        Assert.assertTrue(config.isPluginEnable());
    }

    private NbasetCpConfig createConfig(String pluginEnable) {
        Properties properties = new Properties();
        properties.put(NbasetCpConfig.NBASET_CP_PLUGIN_ENABLE, pluginEnable);

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);
        return new NbasetCpConfig(profilerConfig);
    }


}