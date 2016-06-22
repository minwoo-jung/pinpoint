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
public class AppAuthUserGroup {
    private String number;
    private String applicationId;
    private String userGroupId;
    private String role;
    
    public AppAuthUserGroup() {
    }
    
    public AppAuthUserGroup(String number, String applicationId, String userGroupId, String role) {
        this.number = number;
        this.applicationId = applicationId;
        this.userGroupId = userGroupId;
        this.role = role;
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


    public String getRole() {
        return role;
    }


    public void setRole(String authority) {
        this.role = authority;
    }


    public enum RoleLevel {
        MANAGER("manager"), USER("user"), VISITOR("visitor");
        
        private String name;
        RoleLevel(String name) {
            this.name = name;
        }
        
        public String getName() {
            return this.name;
        }
        
        public String toString() {
            return this.name;
        }
        
    }
}
