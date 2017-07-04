/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.cp.nbasetcp;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Collections;

/**
 * @author Taejin Koo
 */
public class NbasetCpUrlParser implements JdbcUrlParserV2 {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public DatabaseInfo parse(String jdbcUrl) {
        if (jdbcUrl == null) {
            logger.info("jdbcUrl must not be null");
            return UnKnownDatabaseInfo.INSTANCE;
        }

        if (NbasetCpConstants.CP_URL.equals(jdbcUrl)) {
            DatabaseInfo databaseInfo = new DefaultDatabaseInfo(NbasetCpConstants.SERVICE_TYPE, ServiceType.UNKNOWN_DB_EXECUTE_QUERY, jdbcUrl, jdbcUrl, Collections.<String>emptyList(), NbasetCpConstants.CP_NAME);
            return databaseInfo;
        }

        return null;
    }

    @Override
    public ServiceType getServiceType() {
        return NbasetCpConstants.SERVICE_TYPE;
    }

}
