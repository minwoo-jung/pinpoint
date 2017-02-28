/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdbc.nbaset;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.StringMaker;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class NbasetJdbcUrlParser implements JdbcUrlParserV2 {

    private static final String URL_PREFIX = "jdbc:nbase//";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String jdbcUrl) {
        if (jdbcUrl == null) {
            logger.warn("jdbcUrl may not be null");
            return UnKnownDatabaseInfo.INSTANCE;
        }
        if (!jdbcUrl.startsWith(URL_PREFIX)) {
            logger.warn("jdbcUrl has invalid prefix.(url:{}, prefix:{})", jdbcUrl, URL_PREFIX);
            return UnKnownDatabaseInfo.INSTANCE;
        }

        DatabaseInfo result = null;
        try {
            result = parse0(jdbcUrl);
        } catch (Exception e) {
            logger.warn("NbasetJdbcUrl parse error. url: " + jdbcUrl + " Caused: " + e.getMessage(), e);
            result = UnKnownDatabaseInfo.createUnknownDataBase(NbasetConstants.NBASET, NbasetConstants.NBASET_EXECUTE_QUERY, jdbcUrl);
        }
        return result;
    }

    private DatabaseInfo parse0(String jdbcUrl) {
        final StringMaker maker = new StringMaker(jdbcUrl);
        final String value = maker.after(URL_PREFIX).value();
        final String[] tokens = value.split("/");
        if(tokens == null || tokens.length < 3 || tokens[0].isEmpty() || tokens[1].isEmpty()) {
            throw new IllegalArgumentException();
        }

        final List<String> hostList = new ArrayList<String>(1);
        hostList.add(tokens[0]);
        final String databaseId = tokens[1];
        final String normalizedUrl = maker.clear().before('?').value();

        return new DefaultDatabaseInfo(NbasetConstants.NBASET, NbasetConstants.NBASET_EXECUTE_QUERY, jdbcUrl, normalizedUrl, hostList, databaseId);
    }

    @Override
    public ServiceType getServiceType() {
        return NbasetConstants.NBASET;
    }

}