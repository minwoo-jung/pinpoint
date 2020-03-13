package com.navercorp.pinpoint.manager.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

public class RewriteFilter extends GenericFilterBean {

    private static final Log logger = LogFactory.getLog(RewriteFilter.class);
    public static final String DEFAULT_INDEX = "/index.html";
    private static boolean isDebug = logger.isDebugEnabled();

    private static final char PATH_DELIMITER = '/';

    private final String[] rewriteTargetArray = {
            "/main",
    };

    private final String PINPOINT_REST_API_SUFFIX = ".pinpoint";

    private final boolean enable;

    public RewriteFilter(boolean enable) {
        this.enable = enable;
    }

    public void destroy() {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (enable) {
            HttpServletRequest req = (HttpServletRequest) request;
            String requestURI = req.getRequestURI();

            if (isRedirectTarget(requestURI)) {
                HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest) request);
                RequestDispatcher dispatcher = wrapper.getRequestDispatcher(DEFAULT_INDEX);

                if (isDebug) {
                    logger.debug("requestUri:" + requestURI + " ->(forward) " + DEFAULT_INDEX );
                }

                dispatcher.forward(request, response);
            } else {
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }

    }

    private boolean isRedirectTarget(String uri) {
        if (uri == null) {
            return false;
        }

        if (uri.endsWith(PINPOINT_REST_API_SUFFIX)) {
            return false;
        }

        for (String rewriteTarget : rewriteTargetArray) {
            if (uri.equals(rewriteTarget)) {
                return true;
            }
            if (uri.startsWith(rewriteTarget + PATH_DELIMITER)) {
                return true;
            }
        }
        return false;
    }

}
