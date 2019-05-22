/*
 * Copyright 2017 NAVER Corp.
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class UserConfiguration {
    public final static String EMPTY_USERID = "";
    private final static List<ApplicationModel> EMPTY_APPLICATION_MODEL_LIST = new ArrayList<>(0);
    private final static List<InspectorChart> EMPTY_INSPECTION_CHART_LIST = new ArrayList<>(0);
    private String userId = EMPTY_USERID;
    private List<ApplicationModel> favoriteApplications = EMPTY_APPLICATION_MODEL_LIST;
    private List<InspectorChart> applicationInspectorCharts = EMPTY_INSPECTION_CHART_LIST;


    private List<InspectorChart> agentInspectorCharts = EMPTY_INSPECTION_CHART_LIST;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setFavoriteApplications(List<ApplicationModel> favoriteApplications) {
        this.favoriteApplications = favoriteApplications;
    }

    public List<ApplicationModel> getFavoriteApplications() {
        return favoriteApplications;
    }

    public List<InspectorChart> getApplicationInspectorCharts() {
        return applicationInspectorCharts;
    }

    public void setApplicationInspectorCharts(List<InspectorChart> applicationInspectorCharts) {
        this.applicationInspectorCharts = applicationInspectorCharts;
    }

    public List<InspectorChart> getAgentInspectorCharts() {
        return agentInspectorCharts;
    }

    public void setAgentInspectorCharts(List<InspectorChart> agentInspectorCharts) {
        this.agentInspectorCharts = agentInspectorCharts;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserConfiguration{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", favoriteApplications=").append(favoriteApplications);
        sb.append(", applicationInspectorCharts=").append(applicationInspectorCharts);
        sb.append(", agentInspectorCharts=").append(agentInspectorCharts);
        sb.append('}');
        return sb.toString();
    }
}
