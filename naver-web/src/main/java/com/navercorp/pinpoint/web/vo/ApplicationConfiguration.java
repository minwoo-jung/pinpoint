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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class ApplicationConfiguration {
    private String applicationId;
    private AppAuthConfiguration appAuthConfig;
    private Map<String, AppAuthUserGroup> appAuthUserGroups;

    public ApplicationConfiguration(String applicationId, AppAuthConfiguration appAuthConfig, List<AppAuthUserGroup> appAuthUserGroupList) {
        this.applicationId = applicationId;
        this.appAuthConfig = appAuthConfig;
        this.appAuthUserGroups = new HashMap<String, AppAuthUserGroup>();

        for (AppAuthUserGroup appAuthUserGroup : appAuthUserGroupList) {
            this.appAuthUserGroups.put(appAuthUserGroup.getUserGroupId(), appAuthUserGroup);
        }
    }

    public String getApplicationId() {
        return applicationId;
    }

    public AppAuthConfiguration getAppAuthConfiguration() {
        return this.appAuthConfig;
    }

    public boolean isAffiliatedAppUserGroup(List<UserGroup> userGroupList) {
        for(UserGroup userGroup : userGroupList) {
            if(appAuthUserGroups.get(userGroup.getId()) != null) {
                return true;
            }
        }
        
        return false;
    }
}
