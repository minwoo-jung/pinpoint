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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.exception.AuthorityException;
import com.navercorp.pinpoint.web.view.AppUserGroupAuthSerializer;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author minwoo.jung
 *
 */
@JsonSerialize(using = AppUserGroupAuthSerializer.class)
public class AppUserGroupAuth {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private String number;
    private String applicationId;
    private String userGroupId;
    private String positionName;
    private String configurationString;
    private AppAuthConfiguration configuration;
    
    public AppUserGroupAuth() {
    }
    
    public AppUserGroupAuth(String applicationId, String userGroupId, String positionName, AppAuthConfiguration appAuthConfig) {
        this.applicationId = applicationId;
        this.userGroupId = userGroupId;
        this.positionName = positionName;
        this.configuration = appAuthConfig;
        try {
            this.configurationString = OBJECT_MAPPER.writeValueAsString(appAuthConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setConfiguration(AppAuthConfiguration configuration) {
        this.configuration = configuration;
        try {
            this.configurationString = OBJECT_MAPPER.writeValueAsString(configuration);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setConfigurationString(String configurationString) {
        this.configurationString = configurationString;
        try {
            configuration = OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(configurationString, AppAuthConfiguration.class);
        } catch (Exception e) {
            throw new AuthorityException("Can not load authorization configuration of application", e);
        }
    }
    
    public String getConfigurationString(String configurationString) {
        return this.configurationString;
    }

    public String getApplicationId() {
        return applicationId;
    }
    
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }

    public Position getPosition() {
        return Position.findPosition(positionName);
    }
    
    public void setPosition(String positionName) {
        this.positionName = positionName;
    }
    
    public AppAuthConfiguration getConfiguration(){
        return configuration;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppUserGroupAuth{");
        sb.append(", applicationId='").append(applicationId).append('\'');
        sb.append(", userGroupId='").append(userGroupId).append('\'');
        sb.append(", positionName='").append(positionName).append('\'');
        sb.append(", configurationString='").append(configurationString).append('\'');
        sb.append(", configuration=").append(configuration);
        sb.append('}');
        return sb.toString();
    }

    public enum Position {
        GUEST("guest", 1), USER("user", 2), MANAGER("manager", 3);
        
        private final String name;
        private final int level;

        private static final Set<Position> POSITIONS = EnumSet.allOf(Position.class);
        
        Position(String name, int level) {
            this.name = name;
            this.level = level;
        }
        
        public String getName() {
            return this.name;
        }
        
        public int getLevel() {
            return level;
        }
        
        public String toString() {
            return this.name;
        }
        
        public boolean isHigherOrEqualLevel(Position position) {
            if (position == null) {
                return true;
            }
            
            if (this.level >= position.getLevel()) {
                return true;
            } else {
                return false;
            }
        }
        
        public static Position findPosition(String positionName){

            for (Position position : POSITIONS) {
                if(position.getName().equals(positionName)){
                    return position;
                }
            }
            throw new RuntimeException("There was no match for is positionName : " + positionName );
        }
        
    }
}
