/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.web.vo;

/**
 * @author minwoo.jung
 *
 */
public class AppAuthConfiguration {
    
    private final boolean apiMetaData;
    private final boolean sqlMetaData;
    private final boolean paramMetaData;
    private final boolean serverMapData;

    public AppAuthConfiguration() {
        this(false, false, false, false);
    }

    public AppAuthConfiguration(boolean apiMetaData, boolean sqlMetaData, boolean paramMetaData, boolean serverMapData) {
        this.apiMetaData = apiMetaData;
        this.sqlMetaData = sqlMetaData;
        this.paramMetaData = paramMetaData;
        this.serverMapData = serverMapData;
    }

    public boolean getSqlMetaData() {
        return sqlMetaData;
    }


    public boolean getServerMapData() {
        return serverMapData;
    }


    public boolean getApiMetaData() {
        return apiMetaData;
    }


    public boolean getParamMetaData() {
        return paramMetaData;
    }



    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppAuthConfiguration{");
        sb.append("apiMetaData=").append(apiMetaData);
        sb.append(", sqlMetaData=").append(sqlMetaData);
        sb.append(", paramMetaData=").append(paramMetaData);
        sb.append(", serverMapData=").append(serverMapData);
        sb.append('}');
        return sb.toString();
    }
}