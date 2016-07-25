package com.navercorp.pinpoint.web.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class CustomFilter extends AbstractPreAuthenticatedProcessingFilter {

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

}
