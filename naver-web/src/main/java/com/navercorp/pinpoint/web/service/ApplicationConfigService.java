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
package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.vo.AppUserGroupAuth.Role;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;
import com.navercorp.pinpoint.web.vo.User;

import java.util.List;

/**
 * @author minwoo.jung
 */
public interface ApplicationConfigService {

    ApplicationConfiguration selectApplicationConfiguration(String applicationId);

    Role searchMyRole(String applicationId, String userId);

    boolean canInsertConfiguration(AppUserGroupAuth appUserGroupAuth, String userId);

    boolean canEditConfiguration(String applicationId, String userId);

    void updateAppUserGroupAuth(AppUserGroupAuth appUserGroupAuth);

    void deleteAppUserGroupAuth(AppUserGroupAuth appUserGroupAuth);

    void insertAppUserGroupAuth(AppUserGroupAuth appUserGroupAuth);

    void initApplicationConfiguration(String applicationId);

    List<User> selectManagerByUserId(String userId);
}
