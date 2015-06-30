package com.navercorp.pinpoint.plugin.nelo;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class NeloPluginConfig {

    private final boolean log4jLoggingTransactionInfo;

    public NeloPluginConfig(ProfilerConfig config) {
        this.log4jLoggingTransactionInfo = config.readBoolean("profiler.log4j.logging.transactioninfo", false);
    }
    
    public boolean isLog4jLoggingTransactionInfo() {
        return log4jLoggingTransactionInfo;
    }
    

}
