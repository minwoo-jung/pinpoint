/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
@Configuration
public class NaverBatchContextConfiguration implements EnvironmentAware {

    private static final String BATCH_PROFILE = "batch";
    private static final String BATCH_CONFIGURATION_LOCATION = "classpath:profile-%s/batch.properties";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Environment environment;

    @Bean("batchConfigLocation")
    public String batchConfigLocation() {
        String activeProfile = getActiveProfile();
        String location = String.format(BATCH_CONFIGURATION_LOCATION, activeProfile);
        logger.info("batchConfigLocation:{}", location);
        return location;
    }

    @PostConstruct
    public void log() {
        logger.info("batchConfigLocation:{}", batchConfigLocation());
    }

    private String getActiveProfile() {
        final String[] activeProfiles = environment.getActiveProfiles();
        final List<String> activeProfileList = new ArrayList<>(Arrays.asList(activeProfiles));
        activeProfileList.remove(BATCH_PROFILE);
        return activeProfileList.get(0);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
