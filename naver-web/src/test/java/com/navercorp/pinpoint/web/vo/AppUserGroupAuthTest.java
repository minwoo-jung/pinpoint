package com.navercorp.pinpoint.web.vo;

import static org.junit.Assert.*;

import org.junit.Test;

import com.navercorp.pinpoint.web.vo.AppUserGroupAuth.Role;

public class AppUserGroupAuthTest {

    @Test
    public void compareTest() {
        Role role1 = Role.GUEST;
        Role role2 = Role.MANAGER;
        
        assertFalse(role1.isHigherOrEqualLevel(role2));
        assertTrue(role2.isHigherOrEqualLevel(role1));
    }
    
    @Test
    public void findTest() {
        String roleName = "manager";
        assertEquals(Role.MANAGER, Role.findRole(roleName));
    }
    

}
