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
package com.navercorp.pinpoint.web.dao.mysql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.web.dao.UserConfigDao;
import com.navercorp.pinpoint.web.vo.UserConfiguration;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author minwoo.jung
 */
@Repository
public class MysqlUserConfigDao implements UserConfigDao {
    private static final String NAMESPACE = UserConfigDao.class.getPackage().getName() + "." + UserConfigDao.class.getSimpleName() + ".";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String USER_ID = "userId";
    private static final String CONFIGURATION = "configuration";

    @Autowired
    @Qualifier("sqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public void insertUserConfiguration(UserConfiguration userConfiguration) {
        Map<String, String> userConfig = new HashMap<>();
        userConfig.put(USER_ID, userConfiguration.getUserId());
        userConfig.put(CONFIGURATION, convertJson(userConfiguration));
        sqlSessionTemplate.insert(NAMESPACE + "insertUserConfiguration", userConfig);
    }

    @Override
    public void deleteUserConfiguration(String userId) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteUserConfiguration", userId);
    }

    @Override
    public UserConfiguration selectUserConfiguration(String userId) {
        String configuration = sqlSessionTemplate.selectOne(NAMESPACE + "selectUserConfiguration", userId);

        return convertUserConfiguration(configuration);
    }

    @Override
    public void updateUserConfiguration(UserConfiguration userConfiguration) {
        Map<String, String> userConfig = new HashMap<>();
        userConfig.put(USER_ID, userConfiguration.getUserId());
        userConfig.put(CONFIGURATION, convertJson(userConfiguration));
        sqlSessionTemplate.update(NAMESPACE + "updateUserConfiguration", userConfig);
    }

    private String convertJson(UserConfiguration userConfiguration) {
        try {
            return OBJECT_MAPPER.writeValueAsString(userConfiguration);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private UserConfiguration convertUserConfiguration(String userConfiguration) {
        try {
            return OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(userConfiguration, UserConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
