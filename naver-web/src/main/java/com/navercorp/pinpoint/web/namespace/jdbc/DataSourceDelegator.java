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

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author minwoo.jung
 */
public class DataSourceDelegator implements DataSource {

    private final DataSource dataSource;

    private final DelegatorConnectionFactory delegatorConnectionFactory;

    public DataSourceDelegator(DataSource dataSource, DelegatorConnectionFactory delegatorConnectionFactory) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        this.delegatorConnectionFactory = Objects.requireNonNull(delegatorConnectionFactory, "delegatorConnectionFactory");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return dataSource.unwrap(iface);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        return wrapConnection(connection);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = dataSource.getConnection(username, password);
        return wrapConnection(connection);
    }

    private Connection wrapConnection(Connection connection) throws SQLException {
        return delegatorConnectionFactory.createConnection(connection);
    }

    @Override
    public String toString() {
        return "DataSourceDelegator{" +
                "dataSource=" + dataSource +
                ", delegatorConnectionFactory=" + delegatorConnectionFactory +
                '}';
    }
}
