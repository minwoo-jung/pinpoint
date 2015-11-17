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

import org.springframework.util.StringUtils;

import com.navercorp.pinpoint.web.applicationmap.ServerInstance;

/**
 * @author minwoo.jung <minwoo.jung@navercorp.com>
 */
public class NmsMatcherGroup extends MatcherGroup {
    
    private final boolean enable;
    
    public NmsMatcherGroup(String isEnable) {
        this.enable = Boolean.parseBoolean(isEnable);
    }

    public boolean ismatchingType(Object data) {
        if (enable == false) {
            return false;
        }
        
        if (data instanceof ServerInstance) {
            return !StringUtils.isEmpty(((ServerInstance)data).getIp());
        }
        
        return false;
    }

    @Override
    protected String getMatchingSource(Object data) {
        if (data instanceof ServerInstance) {
            return ((ServerInstance)data).getIp();
        }
        
        return null;
    }
}
