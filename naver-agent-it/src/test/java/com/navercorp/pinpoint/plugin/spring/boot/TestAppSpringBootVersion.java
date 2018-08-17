/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.boot;

import java.util.Arrays;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public enum TestAppSpringBootVersion {
    PRE_1_4("1.3.3.RELEASE") {
        @Override
        public List<String> getNestedLibs() {
            return Arrays.asList(
                    "spring-boot-starter-1.3.3.RELEASE.jar",
                    "spring-boot-1.3.3.RELEASE.jar",
                    "spring-context-4.2.5.RELEASE.jar",
                    "spring-aop-4.2.5.RELEASE.jar",
                    "aopalliance-1.0.jar",
                    "spring-beans-4.2.5.RELEASE.jar",
                    "spring-expression-4.2.5.RELEASE.jar",
                    "spring-boot-autoconfigure-1.3.3.RELEASE.jar",
                    "spring-boot-starter-logging-1.3.3.RELEASE.jar",
                    "logback-classic-1.1.5.jar",
                    "logback-core-1.1.5.jar",
                    "slf4j-api-1.7.16.jar",
                    "jcl-over-slf4j-1.7.16.jar",
                    "jul-to-slf4j-1.7.16.jar",
                    "log4j-over-slf4j-1.7.16.jar",
                    "spring-core-4.2.5.RELEASE.jar",
                    "snakeyaml-1.16.jar"
            );
        }
    },
    POST_1_4("1.4.0.RELEASE") {
        @Override
        public List<String> getNestedLibs() {
            return Arrays.asList(
                    "jcl-over-slf4j-1.7.21.jar",
                    "jul-to-slf4j-1.7.21.jar",
                    "log4j-over-slf4j-1.7.21.jar",
                    "logback-classic-1.1.7.jar",
                    "logback-core-1.1.7.jar",
                    "slf4j-api-1.7.21.jar",
                    "snakeyaml-1.17.jar",
                    "spring-aop-4.3.2.RELEASE.jar",
                    "spring-beans-4.3.2.RELEASE.jar",
                    "spring-boot-1.4.0.RELEASE.jar",
                    "spring-boot-autoconfigure-1.4.0.RELEASE.jar",
                    "spring-boot-starter-1.4.0.RELEASE.jar",
                    "spring-boot-starter-logging-1.4.0.RELEASE.jar",
                    "spring-context-4.3.2.RELEASE.jar",
                    "spring-core-4.3.2.RELEASE.jar",
                    "spring-expression-4.3.2.RELEASE.jar"
            );
        }
    },
    POST_1_5_3("1.4.0.RELEASE") {
        @Override
        public List<String> getNestedLibs() {
            return Arrays.asList(
                    "jcl-over-slf4j-1.7.21.jar",
                    "jul-to-slf4j-1.7.21.jar",
                    "log4j-over-slf4j-1.7.21.jar",
                    "logback-classic-1.1.7.jar",
                    "logback-core-1.1.7.jar",
                    "slf4j-api-1.7.21.jar",
                    "snakeyaml-1.17.jar",
                    "spring-aop-4.3.2.RELEASE.jar",
                    "spring-beans-4.3.2.RELEASE.jar",
                    "spring-boot-1.4.0.RELEASE.jar",
                    "spring-boot-autoconfigure-1.4.0.RELEASE.jar",
                    "spring-boot-starter-1.4.0.RELEASE.jar",
                    "spring-boot-starter-logging-1.4.0.RELEASE.jar",
                    "spring-context-4.3.2.RELEASE.jar",
                    "spring-core-4.3.2.RELEASE.jar",
                    "spring-expression-4.3.2.RELEASE.jar"
            );
        }
    },
    POST_2_0_0("2.0.0.RELEASE") {
        @Override
        public List<String> getNestedLibs() {
            return Arrays.asList(
                    "javax.annotation-api-1.3.2.jar",
                    "jul-to-slf4j-1.7.25.jar",
                    "log4j-api-2.10.0.jar",
                    "log4j-to-slf4j-2.10.0.jar",
                    "logback-classic-1.2.3.jar",
                    "logback-core-1.2.3.jar",
                    "slf4j-api-1.7.25.jar",
                    "snakeyaml-1.19.jar",
                    "spring-aop-5.0.4.RELEASE.jar",
                    "spring-beans-5.0.4.RELEASE.jar",
                    "spring-boot-2.0.0.RELEASE.jar",
                    "spring-boot-autoconfigure-2.0.0.RELEASE.jar",
                    "spring-boot-starter-2.0.0.RELEASE.jar",
                    "spring-boot-starter-logging-2.0.0.RELEASE.jar",
                    "spring-context-5.0.4.RELEASE.jar",
                    "spring-core-5.0.4.RELEASE.jar",
                    "spring-expression-5.0.4.RELEASE.jar",
                    "spring-jcl-5.0.4.RELEASE.jar"
            );
        }
    };

    private static final String ARTIFACT_ID = "spring-boot-test";

    private final String executableVersion;

    TestAppSpringBootVersion(String executableVersion) {
        this.executableVersion = executableVersion;
    }

    public String getExecutableName() {
        return ARTIFACT_ID + "-" + this.executableVersion;
    }

    public abstract List<String> getNestedLibs();
}
