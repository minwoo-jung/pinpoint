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

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.web.dao.ApplicationConfigDao;
import com.navercorp.pinpoint.web.exception.AuthorizationException;
import com.navercorp.pinpoint.web.vo.AppAuthConfiguration;
import com.navercorp.pinpoint.web.vo.AppAuthUserGroup;
import com.navercorp.pinpoint.web.vo.ApplicationConfiguration;

/**
 * @author minwoo.jung
 */
@Service
public class ApplicationConfigServiceImpl implements ApplicationConfigService {

    @Autowired
    ApplicationConfigDao applicationConfigDao;
    
    @Override
    public ApplicationConfiguration selectApplicationConfiguration(String applicationId) {
        String configJson = applicationConfigDao.selectAppAuthConfiguration(applicationId);
        AppAuthConfiguration appAuthConfig;
        
        if (configJson == null) {
            appAuthConfig = new AppAuthConfiguration();
        } else {
            try {
                appAuthConfig = new ObjectMapper().readValue(configJson, AppAuthConfiguration.class);
            } catch (IOException e) {
                throw new AuthorizationException("Can not load authorization configuration of application");
            }
        }
        
        List<AppAuthUserGroup> appAuthUserGroupList = applicationConfigDao.selectAppAuthUserGroupList(applicationId);
        return new ApplicationConfiguration(applicationId, appAuthConfig, appAuthUserGroupList);
        
        
    }

}
