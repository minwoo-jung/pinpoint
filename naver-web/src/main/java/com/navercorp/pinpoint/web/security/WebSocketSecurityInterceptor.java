package com.navercorp.pinpoint.web.security;

import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.web.service.ApplicationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.UserGroup;

@Component
public class WebSocketSecurityInterceptor implements HandshakeInterceptor {

    private static final String SSO_USER = "SSO_USER";
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserGroupService userGroupService;
    
    @Autowired
    private ApplicationConfigService configService;
    
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String userId = request.getHeaders().get(SSO_USER).get(0);
        User user = userService.selectUserByUserId(userId);
        Authentication authentication;
        
        if (user != null) {
            List<UserGroup> userGroups = userGroupService.selectUserGroupByUserId(userId);
            boolean pinpointManager = configService.isManager(userId);
            authentication = new PinpointAuthentication(user.getUserId(), user.getName(), userGroups, true, pinpointManager);
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
