/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.nelo;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author minwoo.jung
 */
public class NeloPluginConfig {

    private final boolean log4jLoggingTransactionInfo;

    public NeloPluginConfig(ProfilerConfig config) {
        this.log4jLoggingTransactionInfo = config.readBoolean("profiler.log4j.logging.transactioninfo", false);
    }
    
    public boolean isLog4jLoggingTransactionInfo() {
        return log4jLoggingTransactionInfo;
    }
    

}
