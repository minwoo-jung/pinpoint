package com.navercorp.pinpoint.web.applicationmap.link;

/**
 * @author emeroad
 */
public class DefaultNSightMatcher implements ServerMatcher {

    public static final String URL = "http://nsight.nhncorp.com/dashboard_server/";
    
    @Override
    public boolean isMatched(String value) {
        return true;
    }

    @Override
    public String getLinkName() {
        return "NSight";
    }

    @Override
    public String getLink(String value) {
        return URL + value;
    }
}
