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

package com.navercorp.pinpoint.plugin.spring.boot.it.war;

import com.navercorp.pinpoint.plugin.spring.boot.SpringBootItBase;

import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class WarLauncherItBase extends SpringBootItBase {

    public static final String EXPECTED_CONTEXT = "Spring Boot (WarLauncher)";

    private static final String WEB_INF = "WEB-INF";

    @Override
    protected List<String> getPackagedLibs() {
        List<String> packagedLibs = super.getPackagedLibs();
        packagedLibs.add(formatNestedEntry(getExecutable(), WEB_INF, "classes"));
        Collections.sort(packagedLibs, String.CASE_INSENSITIVE_ORDER);
        return packagedLibs;
    }

    @Override
    protected String getExtension() {
        return ".war";
    }

    @Override
    protected String getEntryPath() {
        return WEB_INF + "/lib";
    }
}
