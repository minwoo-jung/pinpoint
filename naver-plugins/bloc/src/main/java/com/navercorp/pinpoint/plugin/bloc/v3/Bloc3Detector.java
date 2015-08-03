/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.bloc.v3;

import java.io.File;

import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.resolver.ConditionProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;

/**
 * @author Jongho Moon
 * @author HyunGil Jeong
 *
 */
public class Bloc3Detector implements ApplicationTypeDetector, BlocConstants { 
    
    private static final String REQUIRED_MAIN_CLASS = "org.apache.catalina.startup.Bootstrap";
    
    private static final String REQUIRED_SYSTEM_PROPERTY = "catalina.home";
    
    private static final String REQUIRED_CLASS = "org.apache.catalina.startup.Bootstrap";
    
    @Override
    public ServiceType getApplicationType() {
        return BLOC;
    }

    @Override
    public boolean detect(ConditionProvider provider) {
        if (provider.checkMainClass(REQUIRED_MAIN_CLASS) &&
            provider.checkForClass(REQUIRED_CLASS)) {
            String catalinaHomePath = provider.getSystemPropertyValue(REQUIRED_SYSTEM_PROPERTY);
            return testForBlocEnvironment(catalinaHomePath);
        }
        return false;
    }
    
    private boolean testForBlocEnvironment(String catalinaHome) {
        File bloc3CatalinaJar = new File(catalinaHome + "/server/lib/catalina.jar");
        File bloc3ServletApiJar = new File(catalinaHome + "/common/lib/servlet-api.jar");
        if (bloc3CatalinaJar.exists() && bloc3ServletApiJar.exists()) {
            return true;
        }
        return false;
    }

}
