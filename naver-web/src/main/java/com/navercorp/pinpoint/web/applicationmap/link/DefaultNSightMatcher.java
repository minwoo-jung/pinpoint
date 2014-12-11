package com.navercorp.pinpoint.web.applicationmap.link;

/**
 * @author emeroad
 */
public class DefaultNSightMatcher implements ServerMatcher {

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
        return NSightMatcher.URL + value;
    }
}
