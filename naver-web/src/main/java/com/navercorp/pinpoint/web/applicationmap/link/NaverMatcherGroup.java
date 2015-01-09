package com.navercorp.pinpoint.web.applicationmap.link;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author minwoo.jung <minwoo.jung@navercorp.com>
 */
public class NaverMatcherGroup extends MatcherGroup {
    
    static final String MATCHER_KEY_PREFIX = "site.matcher.key";
    static final String MATCHER_URL_PREFIX = "site.matcher.url";
    static final String LINK_NAME ="Nsight";
    
    public NaverMatcherGroup(final Map<String, String> matcherProps) {
        if (matcherProps != null) {
            Map<String, String> keyMap = new HashMap<String, String>();
            Map<String, String> urlMap = new HashMap<String, String>();
            
            for (Entry<String, String> matcherProp : matcherProps.entrySet()) {
                if (matcherProp.getKey().startsWith(MATCHER_KEY_PREFIX)) {
                    keyMap.put(matcherProp.getKey(), matcherProp.getValue());
                } else if (matcherProp.getKey().startsWith(MATCHER_URL_PREFIX)) {
                    urlMap.put(matcherProp.getKey(), matcherProp.getValue());
                }
            }
            
            int comNameStartIndex = MATCHER_KEY_PREFIX.length() + 1;
            
            for (Entry<String, String> keyInfo : keyMap.entrySet()) {
                String comName = keyInfo.getKey().substring(comNameStartIndex, keyInfo.getKey().length());
                String matcherUrlName = MATCHER_URL_PREFIX + "." + comName;
                
                if (comName.isEmpty() || urlMap.get(matcherUrlName) == null) {
                    continue;
                }
       
                String[] keys = keyInfo.getValue().replaceAll(" ", "").split(",");
                
                for (String key : keys) {
                    addServerMatcher(new PostfixServerMatcher(key, urlMap.get(matcherUrlName), LINK_NAME));
                }
                
            }
        }

        setDefaultMatcher(new DefaultNSightMatcher());
    }

}
