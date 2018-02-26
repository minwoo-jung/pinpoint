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
package com.navercorp.pinpoint.web.namespace.jdbc;

import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author minwoo.jung
 */
public class NaverConnectionCreator implements ConnectionCreator {
    private final String defaultDatabaseName;

    public NaverConnectionCreator(String defaultDatabaseName) {
        if (StringUtils.isEmpty(defaultDatabaseName)) {
            throw new NullPointerException("defaultDatabaseName must not be empty");
        }

        this.defaultDatabaseName = defaultDatabaseName;
    }

    @Override
    public Connection createConnection(Connection connection) throws SQLException {
        connection.setCatalog(defaultDatabaseName);
        return new NaverConnectionDelegator(connection);
    }
}
