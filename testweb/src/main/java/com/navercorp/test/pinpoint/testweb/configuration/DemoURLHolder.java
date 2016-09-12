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
 *
 */

package com.navercorp.test.pinpoint.testweb.configuration;

/**
 * 
 * @author netspider
 * 
 */
public abstract class DemoURLHolder {

    public abstract String getBackendWebURL();

    public abstract String getBackendApiURL();

    public static DemoURLHolder getHolder() {
        try {
            String hostname = java.net.InetAddress.getLocalHost().getHostName();

            if (hostname == null) {
                return new DemoURLHolderLocal();
            }

            if (hostname.endsWith("nhnsystem.com")) {
                return new DemoURLHolderDev();
            } else {
                return new DemoURLHolderLocal();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new DemoURLHolderLocal();
        }
    }
}