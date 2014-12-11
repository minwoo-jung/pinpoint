package com.navercorp.pinpoint.web.applicationmap.link;

/**
 * @author emeroad
 */
public class JapanNSightMatcher implements ServerMatcher {

    public static final String POST_FIX = ".nhnjp.ism";

    // TODO 일본쪽 jsight 이름을 수정할것..
    public static final String URL = "http://nsight.nhncorp.jp/dashboard_server/";

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
