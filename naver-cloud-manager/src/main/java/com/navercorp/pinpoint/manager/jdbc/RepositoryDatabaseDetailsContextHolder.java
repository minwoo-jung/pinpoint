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

package com.navercorp.pinpoint.manager.jdbc;

import org.springframework.core.NamedThreadLocal;

/**
 * @author HyunGil Jeong
 */
public class RepositoryDatabaseDetailsContextHolder {

    private static final ThreadLocal<RepositoryDatabaseDetails> repositoryDatabaseDetailsHolder = new NamedThreadLocal<>("Repository Database Details");

    public static void resetRepositoryDatabaseDetails() {
        repositoryDatabaseDetailsHolder.remove();
    }

    public static void setRepositoryDatabaseDetails(RepositoryDatabaseDetails repositoryDatabaseDetails) {
        repositoryDatabaseDetailsHolder.set(repositoryDatabaseDetails);
    }

    public static RepositoryDatabaseDetails getRepositoryDatabaseDetails() {
        return repositoryDatabaseDetailsHolder.get();
    }

    public static RepositoryDatabaseDetails currentRepositoryDatabaseDetails() {
        RepositoryDatabaseDetails repositoryDatabaseDetails = repositoryDatabaseDetailsHolder.get();
        if (repositoryDatabaseDetails == null) {
            throw new IllegalStateException("No thread-bound repository database details found");
        }
        return repositoryDatabaseDetails;
    }
}
