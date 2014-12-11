package com.navercorp.pinpoint.web.applicationmap.link;

/**
 * @author emeroad
 */
public class NSightMatcher implements ServerMatcher {

    public static final String POST_FIX = ".nhnsystem.com";

    public static final String URL = "http://nsight.nhncorp.com/dashboard_server/";

    @Override
    public boolean isMatched(String value) {
        if (value == null) {
            return false;
        }
        return value.endsWith(POST_FIX);
    }

    @Override
    public String getLinkName() {
        return "NSight";
    }

    @Override
    public String getLink(String value) {

        final int index = value.lastIndexOf(POST_FIX);
        if (index == -1) {
            throw new IllegalArgumentException("invalid serverName:" + value);
        }
        String hostName = value.substring(0, index);
        return URL + hostName;
    }
}
