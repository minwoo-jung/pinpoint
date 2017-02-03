/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.dao.props;

import com.navercorp.pinpoint.web.dao.NssAuthDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * @author HyunGil Jeong
 */
public class PropsNssAuthDao implements NssAuthDao {

    private final Collection<String> overrideUserIds;

    private final Collection<String> authorizedPrefixes;

    @Autowired
    public PropsNssAuthDao(
            @Value("#{pinpointWebProps['nss.override.id'] ?: ''}") String overrideIds,
            @Value("#{pinpointWebProps['nss.authorized.prefix'] ?: ''}") String authorizedPrefixes) {
        this.overrideUserIds = StringUtils.commaDelimitedListToSet(overrideIds);
        this.authorizedPrefixes = StringUtils.commaDelimitedListToSet(authorizedPrefixes);
    }

    @Override
    public Collection<String> selectAuthorizedPrefix() {
        return authorizedPrefixes;
    }

    @Override
    public int insertAuthorizedPrefix(String authorizedPrefix) {
        throw new UnsupportedOperationException("Operation not supported when using configuration from properties.");
    }

    @Override
    public int deleteAuthorizedPrefix(String authorizedPrefix) {
        throw new UnsupportedOperationException("Operation not supported when using configuration from properties.");
    }

    @Override
    public Collection<String> selectOverrideUserId() {
        return overrideUserIds;
    }

    @Override
    public int insertOverrideUserId(String userId) {
        throw new UnsupportedOperationException("Operation not supported when using configuration from properties.");
    }

    @Override
    public int deleteOverrideUserId(String userId) {
        throw new UnsupportedOperationException("Operation not supported when using configuration from properties.");
    }
}
