package com.navercorp.pinpoint.web.vo;

public class ApplicationAuthority {
    private String number;
    private String applicationId;
    private String userGroupId;
    private String authority;
    
    public ApplicationAuthority() {
    }
    
    public ApplicationAuthority(String number, String applicationId, String userGroupId, String authority) {
        this.number = number;
        this.applicationId = applicationId;
        this.userGroupId = userGroupId;
        this.authority = authority;
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


    public String getAuthority() {
        return authority;
    }


    public void setAuthority(String authority) {
        this.authority = authority;
    }


    public enum AuthorityLevel {
        MANAGER("manager"), USER("user"), VISITOR("visitor");
        
        private String name;
        AuthorityLevel(String name) {
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
