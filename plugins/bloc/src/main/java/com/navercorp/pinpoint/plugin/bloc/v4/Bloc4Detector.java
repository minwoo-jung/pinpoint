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

import java.io.File;

import com.navercorp.pinpoint.bootstrap.plugin.ServerTypeDetector;
import com.navercorp.pinpoint.common.util.SystemProperty;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;

/**
 * @author Jongho Moon
 *
 */
public class Bloc4Detector implements ServerTypeDetector, BlocConstants {
    
    @Override
    public String getServerTypeName() {
        return SERVER_TYPE_BLOC;
    }

    @Override
    public boolean detect() {
        String blocHome = SystemProperty.INSTANCE.getProperty("bloc.home");
        
        if (blocHome != null) {
            File home = new File(blocHome);
            
            if (home.exists() && home.isDirectory()) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean canOverride(String serverType) {
        return false;
    }
}
