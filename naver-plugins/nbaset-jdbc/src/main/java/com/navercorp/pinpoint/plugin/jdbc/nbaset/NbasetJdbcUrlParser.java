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
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParser;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.StringMaker;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class NbasetJdbcUrlParser extends JdbcUrlParser {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo doParse(String url) {
        if (url == null) {
            logger.info("Not found nbase-t url={}", url);
            return UnKnownDatabaseInfo.createUnknownDataBase(NbasetConstants.NBASET, NbasetConstants.NBASET_EXECUTE_QUERY, null);
        }

        final StringMaker maker = new StringMaker(url);
        final String value = maker.after("jdbc:nbase//").value();
        if(value == null) {
            logger.info("Invalid nbase-t url={}", url);
            return UnKnownDatabaseInfo.createUnknownDataBase(NbasetConstants.NBASET, NbasetConstants.NBASET_EXECUTE_QUERY, null);
        }

        final String[] tokens = value.split("/");
        if(tokens == null || tokens.length < 3 || tokens[0].isEmpty() || tokens[1].isEmpty()) {
            logger.info("Invalid nbase-t url={}", url);
            return UnKnownDatabaseInfo.createUnknownDataBase(NbasetConstants.NBASET, NbasetConstants.NBASET_EXECUTE_QUERY, null);
        }

        final List<String> hostList = new ArrayList<String>(1);
        hostList.add(tokens[0]);
        final String databaseId = tokens[1];
        final String normalizedUrl = maker.clear().before('?').value();

        return new DefaultDatabaseInfo(NbasetConstants.NBASET, NbasetConstants.NBASET_EXECUTE_QUERY, url, normalizedUrl, hostList, databaseId);
    }
}