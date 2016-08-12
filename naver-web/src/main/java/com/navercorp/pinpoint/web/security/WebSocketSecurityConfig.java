package com.navercorp.pinpoint.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    
    @Autowired
    HandshakeInterceptor handshakeInterceptor;

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages.anyMessage().authenticated();
        
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/**.pinpointws").addInterceptors(handshakeInterceptor);
    }
}
