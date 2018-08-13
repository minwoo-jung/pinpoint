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

package com.navercorp.pinpoint.plugin.spring.boot.it.jar;

import com.navercorp.pinpoint.plugin.spring.boot.SpringBootItBase;
import com.navercorp.pinpoint.plugin.spring.boot.TestAppSpringBootVersion;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class JarLauncherItBase extends SpringBootItBase {

    public static final String EXPECTED_CONTEXT = "Spring Boot (JarLauncher)";

    @Override
    protected List<String> getPackagedLibs() {
        List<String> packagedLibs = super.getPackagedLibs();
        // 1.4+ adds BOOT-INF/classes directory
        if (getTestAppSpringBootVersion() == TestAppSpringBootVersion.POST_1_4) {
            packagedLibs.add(formatNestedEntry(getExecutable(), BOOT_INF, "classes"));
        }
        return packagedLibs;
    }

    @Override
    protected String getExtension() {
        return ".jar";
    }

    @Override
    protected String getEntryPath() {
        // 1.4+ creates a separate BOOT-INF directory to package libraries
        if (getTestAppSpringBootVersion() == TestAppSpringBootVersion.POST_1_4) {
            return BOOT_INF + "/lib";
        } else {
            return "lib";
        }
    }
}
