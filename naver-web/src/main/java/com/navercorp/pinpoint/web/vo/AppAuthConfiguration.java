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
    
    private boolean apiMetaData = false;
    private boolean sqlMetaData = false;
    private boolean paramMetaData = false;
    private boolean serverMapData = false;

    public boolean getSqlMetaData() {
        return sqlMetaData;
    }
    
    public void setSqlMetaData(boolean sqlMetaData) {
        this.sqlMetaData = sqlMetaData;
    }

    public boolean getServerMapData() {
        return serverMapData;
    }

    public void setServerMapData(boolean serverMapData) {
        this.serverMapData = serverMapData;
    }

    public boolean getApiMetaData() {
        return apiMetaData;
    }

    public void setApiMetaData(boolean apiMetaData) {
        this.apiMetaData = apiMetaData;
    }

    public boolean getParamMetaData() {
        return paramMetaData;
    }

    public void setParamMetaData(boolean paramMetaData) {
        this.paramMetaData = paramMetaData;
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
