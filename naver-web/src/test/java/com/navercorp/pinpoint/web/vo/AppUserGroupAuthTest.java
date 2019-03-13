package com.navercorp.pinpoint.web.vo;

import static org.junit.Assert.*;

import org.junit.Test;

import com.navercorp.pinpoint.web.vo.AppUserGroupAuth.Position;

public class AppUserGroupAuthTest {

    @Test
    public void compareTest() {
        Position position1 = AppUserGroupAuth.Position.GUEST;
        Position position2 = AppUserGroupAuth.Position.MANAGER;
        
        assertFalse(position1.isHigherOrEqualLevel(position2));
        assertTrue(position2.isHigherOrEqualLevel(position1));
    }
    
    @Test
    public void findTest() {
        String roleName = "manager";
        assertEquals(Position.MANAGER, AppUserGroupAuth.Position.findPosition(roleName));
    }
    

}
