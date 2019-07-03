/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.manager.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author HyunGil Jeong
 */
@Service
@Profile("local")
public class ManagementInMemoryLockService implements ManagementLockService {

    private final ConcurrentMap<String, Synchronizer> organizationLocks = new ConcurrentHashMap<>();

    @Override
    public boolean acquire(String organizationKey, UUID monitor) {
        Synchronizer synchronizer = organizationLocks.computeIfAbsent(organizationKey, k -> new Synchronizer(monitor));
        if (synchronizer.monitor.equals(monitor)) {
            synchronizer.counter.incrementAndGet();
            return true;
        }
        return false;
    }

    @Override
    public void release(String organizationKey, UUID monitor) {
        organizationLocks.computeIfPresent(organizationKey, (k, v) -> {
            if (!v.monitor.equals(monitor)) {
                return v;
            }
            int counter = v.counter.decrementAndGet();
            if (counter < 1) {
                return null;
            }
            return v;
        });
    }

    @Override
    public boolean reset(String organizationKey) {
        Synchronizer prev = organizationLocks.remove(organizationKey);
        if (prev != null) {
            return true;
        }
        return false;
    }

    private static class Synchronizer {
        private final UUID monitor;
        private final AtomicInteger counter = new AtomicInteger();

        private Synchronizer(UUID monitor) {
            this.monitor = Objects.requireNonNull(monitor, "monitor must not be null");
        }


    }
}
