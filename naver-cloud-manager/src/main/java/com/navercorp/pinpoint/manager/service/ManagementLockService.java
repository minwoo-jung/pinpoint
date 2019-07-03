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

import java.util.UUID;

/**
 * This might not be needed if repository state is persisted.
 * If lock service is required for schema updates, consider using this.
 *
 * @author HyunGil Jeong
 */
public interface ManagementLockService {

    boolean acquire(String organizationKey, UUID monitor);

    void release(String organizationKey, UUID monitor);

    boolean reset(String organizationKey);
}
