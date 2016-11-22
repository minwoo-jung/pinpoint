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

/**
 * @author minwoo.jung
 *
 */
@JsonSerialize(using = AppUserGroupAuthSerializer.class)
public class AppUserGroupAuth {
    private String number;
    private String applicationId;
    private String userGroupId;
    private String roleName;
    private String configurationString;
    private AppAuthConfiguration configuration;
    
    public AppUserGroupAuth() {
    }
    
    public AppUserGroupAuth(String applicationId, String userGroupId, String roleName, AppAuthConfiguration appAuthConfig) {
        this.applicationId = applicationId;
        this.userGroupId = userGroupId;
        this.roleName = roleName;
        this.configuration = appAuthConfig;
        try {
            this.configurationString = new ObjectMapper().writeValueAsString(appAuthConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setConfiguration(AppAuthConfiguration configuration) {
        this.configuration = configuration;
        try {
            this.configurationString = new ObjectMapper().writeValueAsString(configuration);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setConfigurationString(String configurationString) {
        this.configurationString = configurationString;
        try {
            configuration = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(configurationString, AppAuthConfiguration.class);
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

    public Role getRole() {
        return Role.findRole(roleName);
    }
    
    public void setRole(String roleName) {
        this.roleName = roleName;
    }
    
    public AppAuthConfiguration getConfiguration(){
        return configuration;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("applicationId : " + applicationId + ",");
        sb.append("userGroupId : " + userGroupId + ",");
        sb.append("roleName : " + roleName + ",");
        sb.append("configuration : (" + configuration + ")");
        return sb.toString();
    }

    public enum Role {
        GUEST("guest", 1), USER("user", 2), MANAGER("manager", 3);
        
        private String name;
        private int level;
        
        Role(String name, int level) {
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
        
        public boolean isHigherOrEqualLevel(Role role) {
            if (role == null) {
                return true;
            }
            
            if (this.level >= role.getLevel()) {
                return true;
            } else {
                return false;
            }
        }
        
        public static Role findRole(String roleName){
            for(Role role : values()){
                if(role.getName().equals(roleName)){
                    return role;
                }
            }
            throw new RuntimeException("There was no match for is roleName : " + roleName );
        }
        
    }
}
