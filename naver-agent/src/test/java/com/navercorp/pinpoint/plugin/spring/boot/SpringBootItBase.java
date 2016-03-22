/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.boot;

import com.navercorp.pinpoint.common.Version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public abstract class SpringBootItBase {

    public static final String SERVER_TYPE = "SPRING_BOOT";

    public static final String AGENT_JAR = "pinpoint-bootstrap-" + Version.VERSION + ".jar";

    public static final String CLASSPATH_LIB = "pinpoint-spring-boot-plugin-" + Version.VERSION + ".jar";

    public static final String EXECUTABLE = "spring-boot-test-1.0";

    public static final List<String> NESTED_LIBS = Arrays.asList(
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

    public static final String NESTED_LIB_SEPARATOR = "!/";

    protected List<String> getPackagedLibs() {
        List<String> packagedLibs = new ArrayList<String>();
        packagedLibs.add(getExecutable());
        for (String nestedLib : NESTED_LIBS) {
            packagedLibs.add(formatNestedEntry(getExecutable(), getEntryPath(), nestedLib));
        }
        return packagedLibs;
    }

    protected final String formatNestedEntry(String parent, String entryPath, String nestedEntry) {
        StringBuilder sb = new StringBuilder(parent);
        sb.append(NESTED_LIB_SEPARATOR);
        sb.append(entryPath).append("/");
        sb.append(nestedEntry);
        return sb.toString();
    }

    protected abstract String getExecutable();

    protected abstract String getEntryPath();

    public class ExpectedLibraries {

        private final List<String> expectedLibraries = new ArrayList<String>();

        public ExpectedLibraries withAgentJar() {
            this.expectedLibraries.add(AGENT_JAR);
            return this;
        }

        public ExpectedLibraries withClasspathLib() {
            this.expectedLibraries.add(CLASSPATH_LIB);
            return this;
        }

        public ExpectedLibraries withPackagedLib() {
            this.expectedLibraries.addAll(getPackagedLibs());
            return this;
        }

        public ExpectedLibraries withCustomEntry(String entry) {
            this.expectedLibraries.add(entry);
            return this;
        }

        public List<String> getLibraries() {
            return Collections.unmodifiableList(this.expectedLibraries);
        }
    }

}
