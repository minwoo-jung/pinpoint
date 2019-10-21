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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class ApplicationConfiguration {
    private final String applicationId;
    private final Map<String, AppUserGroupAuth> appUserGroupAuthes;

    public ApplicationConfiguration(String applicationId, List<AppUserGroupAuth> appUserGroupAuthList) {
        this.applicationId = Objects.requireNonNull(applicationId, "applicationId");

        Objects.requireNonNull(appUserGroupAuthList, "appUserGroupAuthList");
        this.appUserGroupAuthes = toMap(appUserGroupAuthList);
    }

    private Map<String, AppUserGroupAuth> toMap(List<AppUserGroupAuth> appUserGroupAuthList) {
        Map<String, AppUserGroupAuth> map = new HashMap<>(appUserGroupAuthList.size());
        for (AppUserGroupAuth appAuthUserGroup : appUserGroupAuthList) {
            map.put(appAuthUserGroup.getUserGroupId(), appAuthUserGroup);
        }
        return map;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public boolean isAffiliatedAppUserGroup(List<UserGroup> userGroupList) {
        for (UserGroup userGroup : userGroupList) {
            if (appUserGroupAuthes.get(userGroup.getId()) != null) {
                return true;
            }
        }
        
        return false;
    }
    
    public Map<String, AppUserGroupAuth> getAppUserGroupAuthes() {
        return appUserGroupAuthes;
    }

    public List<AppUserGroupAuth> getAppUserGroupAuth() {
        return new ArrayList<>(appUserGroupAuthes.values());
    }
}
