/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.link;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;

import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstance;
import com.navercorp.pinpoint.web.applicationmap.link.LinkInfo.LinkType;
import com.navercorp.pinpoint.web.vo.AgentInfo;

/**
 * @author minwoo.jung <minwoo.jung@navercorp.com>
 */
public class NsightMatcherGroup extends MatcherGroup {
    
    static final String MATCHER_KEY_PREFIX = "site.matcher.key.nsight";
    static final String MATCHER_URL_PREFIX = "site.matcher.url.nsight";
    static final String LINK_NAME ="Ns";
    
    public NsightMatcherGroup(final Map<String, String> matcherProps) {
        if (matcherProps != null) {
            Map<String, String> keyMap = new HashMap<>();
            Map<String, String> urlMap = new HashMap<>();
            
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

                String value = keyInfo.getValue();
                final String[] keys = StringUtils.tokenizeToStringArray(value, ",");
                for (String key : keys) {
                    addServerMatcher(new PostfixServerMatcher(key, urlMap.get(matcherUrlName), LINK_NAME, LinkType.ATAG));
                }
                
            }
        }

        setDefaultMatcher(new DefaultNSightMatcher());
    }

    public boolean ismatchingType(Object data) {
        if (data instanceof ServerInstance) {
            return !StringUtils.isEmpty(((ServerInstance)data).getHostName());
        }
        if (data instanceof AgentInfo) {
            return !StringUtils.isEmpty(((AgentInfo)data).getHostName());
        }
        
        return false;
    }

    @Override
    protected String getMatchingSource(Object data) {
        if (data instanceof AgentInfo) {
            return ((AgentInfo)data).getHostName();
        }
        if (data instanceof ServerInstance) {
            return ((ServerInstance)data).getHostName();
        }
        
        return null;
    }

}
