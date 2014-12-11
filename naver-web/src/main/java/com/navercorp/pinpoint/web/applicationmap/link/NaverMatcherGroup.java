package com.navercorp.pinpoint.web.applicationmap.link;

/**
 * @author minwoo.jung <minwoo.jung@navercorp.com>
 *
 */
public class NaverMatcherGroup extends MatcherGroup {
    
    public NaverMatcherGroup() {
        addServerMatcher(new NSightMatcher());
        addServerMatcher(new JapanNSightMatcher());
        setDefaultMatcher(new DefaultNSightMatcher());
    }

}
