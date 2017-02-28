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
import org.junit.Test;

public class NbasetJdbcUrlParserTest {

    @Test
    public void doParse() {
        NbasetJdbcUrlParser parser = new NbasetJdbcUrlParser();
        printInfo(parser.parse("jdbc:nbase//127.0.0.1:6220/dev/dynamic?foo=bar"));
        printInfo(parser.parse("jdbc:nbase//127.0.0.1:6220/dev/query?ckey=ckey&foo=bar"));
        printInfo(parser.parse("jdbc:nbase//127.0.0.1:6220/dev/query-all?foo=bar"));
        printInfo(parser.parse("jdbc:nbase//127.0.0.1:6220/dev/query-all-ckey-list?ckey-list=ckey1;ckey2;ckey3&foo=bar"));

        // invalid
        printInfo(parser.parse(""));
        printInfo(parser.parse("jdbc:nbase///dev/dynamic?foo=bar"));
        printInfo(parser.parse("jdbc:nbase//127.0.0.1:6220/dev"));
    }

    private void printInfo(DatabaseInfo databaseInfo) {
        System.out.println(databaseInfo);
    }

}