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
 *
 */

package com.navercorp.pinpoint.plugin.nelo;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author Woonduk Kang(emeroad)
 */
public class NeloPluginConfig {

    private final boolean log4jLoggingTransactionInfo;
    private final boolean logbackLoggingTransactionInfo;

    public NeloPluginConfig(ProfilerConfig profilerConfig) {
        /**
         * log4j
         */
        this.log4jLoggingTransactionInfo = profilerConfig.readBoolean("profiler.log4j.logging.transactioninfo", false);

        /**
         * logback
         */
        this.logbackLoggingTransactionInfo = profilerConfig.readBoolean("profiler.logback.logging.transactioninfo", false);
    }

    public boolean isLog4jLoggingTransactionInfo() {
        return this.log4jLoggingTransactionInfo;
    }


    public boolean isLogbackLoggingTransactionInfo() {
        return this.logbackLoggingTransactionInfo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NeloPluginConfig{");
        sb.append("log4jLoggingTransactionInfo=").append(log4jLoggingTransactionInfo);
        sb.append(", logbackLoggingTransactionInfo=").append(logbackLoggingTransactionInfo);
        sb.append('}');
        return sb.toString();
    }
}
