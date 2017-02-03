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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author HyunGil Jeong
 */
public class PropsNssAuthDaoTest {

    @Test
    public void nullPropertiesShouldReturnEmptyLists() {
        // Given
        final String configuredOverrideIds = null;
        final String configuredAuthorizedPrefixes = null;
        PropsNssAuthDao dao = new PropsNssAuthDao(configuredOverrideIds, configuredAuthorizedPrefixes);
        // When
        Collection<String> overrideIds = dao.selectOverrideUserId();
        Collection<String> authorizedPrefixes = dao.selectAuthorizedPrefix();
        // Then
        Assert.assertTrue(overrideIds.isEmpty());
        Assert.assertTrue(authorizedPrefixes.isEmpty());
    }

    @Test
    public void emptyPropertiesShouldReturnEmptyLists() {
        // Given
        final String configuredOverrideIds = "";
        final String configuredAuthorizedPrefixes = "";
        PropsNssAuthDao dao = new PropsNssAuthDao(configuredOverrideIds, configuredAuthorizedPrefixes);
        // When
        Collection<String> overrideIds = dao.selectOverrideUserId();
        Collection<String> authorizedPrefixes = dao.selectAuthorizedPrefix();
        // Then
        Assert.assertTrue(overrideIds.isEmpty());
        Assert.assertTrue(authorizedPrefixes.isEmpty());
    }

    @Test
    public void propertiesShouldReturnCommaSeparatedLists() {
        // Given
        final String[] expectedOverrideIds = {"overrideId1", "overrideId2"};
        final String[] expectedAuthorizedPrefixes = {"prefix1", "prefix2"};
        PropsNssAuthDao dao = new PropsNssAuthDao(
                StringUtils.arrayToCommaDelimitedString(expectedOverrideIds),
                StringUtils.arrayToCommaDelimitedString(expectedAuthorizedPrefixes));
        // When
        Collection<String> overrideIds = dao.selectOverrideUserId();
        Collection<String> authorizedPrefixes = dao.selectAuthorizedPrefix();
        // Then
        Assert.assertTrue(overrideIds.containsAll(Arrays.asList(expectedOverrideIds)));
        Assert.assertTrue(authorizedPrefixes.containsAll(Arrays.asList(expectedAuthorizedPrefixes)));
    }
}
