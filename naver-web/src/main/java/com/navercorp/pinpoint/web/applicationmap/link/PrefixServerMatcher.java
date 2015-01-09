package com.navercorp.pinpoint.web.applicationmap.link;

public class PrefixServerMatcher implements ServerMatcher {

    public final String postfix;
    public final String url;
    public final String linkName;
    
    public PrefixServerMatcher(String postfix, String url, String linkName) {
        this.postfix = postfix;
        this.url = url;
        this.linkName = linkName;
    }
    
    @Override
    public boolean isMatched(String value) {
        if (value == null) {
            return false;
        }
        return value.endsWith(postfix);
    }

    @Override
    public String getLinkName() {
        return linkName;
    }

    @Override
    public String getLink(String value) {
        final int index = value.lastIndexOf(postfix);
        
        if (index == -1) {
            throw new IllegalArgumentException("invalid serverName:" + value);
        }
        
        String hostName = value.substring(0, index);
        return url + hostName;
    }
}
