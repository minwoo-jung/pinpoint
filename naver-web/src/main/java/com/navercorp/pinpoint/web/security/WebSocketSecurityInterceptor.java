package com.navercorp.pinpoint.web.security;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.navercorp.pinpoint.web.dao.ApplicationConfigDao;
import com.navercorp.pinpoint.web.security.AutoLoginAuthenticationFilter.CustomHttpServletRequest;
import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserGroup;

@Component
public class WebSocketSecurityInterceptor implements HandshakeInterceptor {

    private String userId = "KR14966";
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserGroupService userGroupService;
    
    @Autowired
    private ApplicationConfigDao configDao;
    
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        User user = userService.selectUserByUserId(userId);
        List<UserGroup> userGroups = userGroupService.selectUserGroupByUserId(userId);
        boolean pinpointManager = configDao.selectExistManager(userId);
        Authentication authentication;
        
        if (user != null) {
            authentication = new PinpointAuthentication(user.getUserId(), user.getName(), userGroups, null, true, pinpointManager);
        } else {
            authentication = new PinpointAuthentication();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        return true;
        
    }

    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
        SecurityContextHolder.clearContext();
    }
}
