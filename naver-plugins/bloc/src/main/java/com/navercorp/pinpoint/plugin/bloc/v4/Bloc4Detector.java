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
package com.navercorp.pinpoint.plugin.bloc.v4;

import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.resolver.ConditionProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;

/**
 * @author Jongho Moon
 * @author HyunGil Jeong
 *
 */
public class Bloc4Detector implements ApplicationTypeDetector, BlocConstants {
    
    private static final String REQUIRED_MAIN_CLASS = "com.nhncorp.lucy.bloc.server.BlocServer";
    
    private static final String REQUIRED_SYSTEM_PROPERTY = "bloc.home";
    
    private static final String REQUIRED_CLASS =  "com.nhncorp.lucy.bloc.server.Bootstrap";
    
    @Override
    public ServiceType getServerType() {
        return BLOC;
    }

    @Override
    public boolean detect(ConditionProvider provider) {
        return provider.checkMainClass(REQUIRED_MAIN_CLASS) &&
               provider.checkSystemProperty(REQUIRED_SYSTEM_PROPERTY) &&
               provider.checkForClass(REQUIRED_CLASS);
    }
    
}
