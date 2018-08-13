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

import com.navercorp.pinpoint.common.Version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public abstract class SpringBootItBase {

    public static final String SERVER_TYPE = "SPRING_BOOT";

    public static final String AGENT_JAR = "pinpoint-bootstrap-" + Version.VERSION + ".jar";

    public static final String CLASSPATH_LIB = "pinpoint-spring-boot-plugin-" + Version.VERSION + ".jar";

    public static final String BOOT_INF = "BOOT-INF";

    public static final String NESTED_LIB_SEPARATOR = "!/";

    protected List<String> getPackagedLibs() {
        List<String> packagedLibs = new ArrayList<String>();
        for (String nestedLib : getTestAppSpringBootVersion().getNestedLibs()) {
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

    protected final String getExecutable() {
        String executableName = getTestAppSpringBootVersion().getExecutableName();
        return executableName + getExtension();
    }

    protected abstract String getExtension();

    protected abstract String getEntryPath();

    protected final TestAppSpringBootVersion getTestAppSpringBootVersion() {
        return this.getClass().getAnnotation(TestAppVersion.class).value();
    }

    public class ExpectedLibraries {

        private final List<String> expectedLibraries = new ArrayList<String>();

        public ExpectedLibraries withExecutable() {
            this.expectedLibraries.add(getExecutable());
            return this;
        }

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
