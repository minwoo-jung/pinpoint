/**
 * Copyright 2016 NAVER Corp.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.jdbc.nbaset;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NbasetJdbcUrlParserTest {

    @Test
    public void doParse() {
        NbasetJdbcUrlParser parser = new NbasetJdbcUrlParser();
        DatabaseInfo databaseInfo = null;

        assertDatabaseInfo(parser.parse("jdbc:nbase//127.0.0.1:6220/dev/dynamic?foo=bar"), NbasetConstants.NBASET, "dev", "127.0.0.1:6220", "jdbc:nbase//127.0.0.1:6220/dev/dynamic?foo=bar", "jdbc:nbase//127.0.0.1:6220/dev/dynamic", true);
        assertDatabaseInfo(parser.parse("jdbc:nbase//127.0.0.1:6220/dev/query?ckey=ckey&foo=bar"), NbasetConstants.NBASET, "dev", "127.0.0.1:6220", "jdbc:nbase//127.0.0.1:6220/dev/query?ckey=ckey&foo=bar", "jdbc:nbase//127.0.0.1:6220/dev/query", true);
        assertDatabaseInfo(parser.parse("jdbc:nbase//127.0.0.1:6220/dev/query-all?foo=bar"), NbasetConstants.NBASET, "dev", "127.0.0.1:6220", "jdbc:nbase//127.0.0.1:6220/dev/query-all?foo=bar", "jdbc:nbase//127.0.0.1:6220/dev/query-all", true);
        assertDatabaseInfo(parser.parse("jdbc:nbase//127.0.0.1:6220/dev/query-all-ckey-list?ckey-list=ckey1;ckey2;ckey3&foo=bar"), NbasetConstants.NBASET, "dev", "127.0.0.1:6220", "jdbc:nbase//127.0.0.1:6220/dev/query-all-ckey-list?ckey-list=ckey1;ckey2;ckey3&foo=bar", "jdbc:nbase//127.0.0.1:6220/dev/query-all-ckey-list", true);
        assertDatabaseInfo(parser.parse("jdbc:nbase//127.0.0.1:6220/dev"), NbasetConstants.NBASET, "dev", "127.0.0.1:6220", "jdbc:nbase//127.0.0.1:6220/dev", "jdbc:nbase//127.0.0.1:6220/dev", true);
        assertDatabaseInfo(parser.parse("jdbc:nbase//127.0.0.1:6220/service/query?port=6220&booleanIsNumberFormat=true&zone-list=0&txTimeout=10"), NbasetConstants.NBASET, "service", "127.0.0.1:6220", "jdbc:nbase//127.0.0.1:6220/service/query?port=6220&booleanIsNumberFormat=true&zone-list=0&txTimeout=10", "jdbc:nbase//127.0.0.1:6220/service/query", true);

        // not found database-id
        assertDatabaseInfo(parser.parse("jdbc:nbase://127.0.0.1:6219?port=6221&timeout=1&txTimeout=10"), NbasetConstants.NBASET, "default", "127.0.0.1:6219", "jdbc:nbase://127.0.0.1:6219?port=6221&timeout=1&txTimeout=10", "jdbc:nbase://127.0.0.1:6219", true);
        assertDatabaseInfo(parser.parse("jdbc:nbase://127.0.0.1:6219"), NbasetConstants.NBASET, "default", "127.0.0.1:6219", "jdbc:nbase://127.0.0.1:6219", "jdbc:nbase://127.0.0.1:6219", true);
        assertDatabaseInfo(parser.parse("jdbc:nbase://127.0.0.1:6219?datetimeIsStringFormat=true"), NbasetConstants.NBASET, "default", "127.0.0.1:6219", "jdbc:nbase://127.0.0.1:6219?datetimeIsStringFormat=true", "jdbc:nbase://127.0.0.1:6219", true);

        assertDatabaseInfo(parser.parse("jdbc:log4jdbc:nbase//xdev063.vpd:6220/zlatan/query?ckey=yj"), NbasetConstants.NBASET, "zlatan", "xdev063.vpd:6220", "jdbc:log4jdbc:nbase//xdev063.vpd:6220/zlatan/query?ckey=yj", "jdbc:log4jdbc:nbase//xdev063.vpd:6220/zlatan/query", true);
        assertDatabaseInfo(parser.parse("jdbc:nbase//xdev063.vpd:6220/zlatan/query"), NbasetConstants.NBASET, "zlatan", "xdev063.vpd:6220", "jdbc:nbase//xdev063.vpd:6220/zlatan/query", "jdbc:nbase//xdev063.vpd:6220/zlatan/query", true);

        // error
        assertDatabaseInfo(parser.parse("jdbc:nbase///dev/dynamic?foo=bar"), NbasetConstants.NBASET, "error", "error", "jdbc:nbase///dev/dynamic?foo=bar", "jdbc:nbase///dev/dynamic?foo=bar", false);
        assertDatabaseInfo(parser.parse("jdbc:nbase//"), NbasetConstants.NBASET, "error", "error", "jdbc:nbase//", "jdbc:nbase//", false);

        // unknown
        assertDatabaseInfo(parser.parse(""), ServiceType.UNKNOWN_DB, "unknown", "unknown", "unknown", "unknown", false);
        assertDatabaseInfo(parser.parse("jdbc://127.0.0.1:6219/dev/dynamic?foo=bar"), ServiceType.UNKNOWN_DB, "unknown", "unknown", "unknown", "unknown", false);
    }

    private void assertDatabaseInfo(DatabaseInfo databaseInfo, ServiceType serviceType, String id, String multipleHost, String realUrl, String url, boolean parsingComplete) {
        assertEquals(serviceType.getCode(), databaseInfo.getType().getCode());
        assertEquals(id, databaseInfo.getDatabaseId());
        assertEquals(multipleHost, databaseInfo.getMultipleHost());
        assertEquals(realUrl, databaseInfo.getRealUrl());
        assertEquals(url, databaseInfo.getUrl());
        assertEquals(parsingComplete, databaseInfo.isParsingComplete());
    }
}