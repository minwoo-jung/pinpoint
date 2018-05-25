/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.memory;

import com.navercorp.pinpoint.collector.dao.NameSpaceDao;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Taejin Koo
 */
@Repository
@Profile("tokenAuthentication")
public class MemoryNameSpaceDao implements NameSpaceDao {

    private final ConcurrentHashMap<String, String> namespaceMap = new ConcurrentHashMap<>();

    @Override
    public boolean create(String userId, String namespace) {
        String oldValue = namespaceMap.putIfAbsent(userId, namespace);
        return oldValue == null;
    }

    @Override
    public String get(String userId) {
        return namespaceMap.get(userId);
    }

}
