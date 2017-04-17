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

import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author HyunGil Jeong
 */
public enum LauncherType {
    JAR("jar", "jar"),
    WAR("war", "war"),
    PROPERTIES("props", "jar");

    private final String path;
    private final String extension;

    LauncherType(String path, String extension) {
        this.path = path;
        this.extension = extension;
    }

    public String getPath(String parent) {
        if (StringUtils.isEmpty(parent)) {
            throw new IllegalArgumentException("parent path must not be empty");
        }
        return parent + "/" + this.path;
    }

    public String getJarName(String jarName) {
        if (StringUtils.isEmpty(jarName)) {
            throw new IllegalArgumentException("jarName must not be empty");
        }
        return jarName + "." + this.extension;
    }
}
