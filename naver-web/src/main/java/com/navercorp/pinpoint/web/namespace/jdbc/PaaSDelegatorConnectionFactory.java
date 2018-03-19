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

import com.navercorp.pinpoint.web.namespace.NameSpaceInfo;
import com.navercorp.pinpoint.web.namespace.NameSpaceInfoFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class PaaSDelegatorConnectionFactory implements DelegatorConnectionFactory {

    private final NameSpaceInfoFactory nameSpaceInfoFactory;

    public PaaSDelegatorConnectionFactory(NameSpaceInfoFactory nameSpaceInfoFactory) {
        this.nameSpaceInfoFactory = Objects.requireNonNull(nameSpaceInfoFactory, "nameSpaceInfoFactory must not be null");
    }

    @Override
    public Connection createConnection(Connection connection) throws SQLException {
        NameSpaceInfo nameSpaceInfo = nameSpaceInfoFactory.getNameSpaceInfo();
        connection.setCatalog(nameSpaceInfo.getMysqlDatabaseName());
        return GuardConnection.wrap(connection);
    }
}
