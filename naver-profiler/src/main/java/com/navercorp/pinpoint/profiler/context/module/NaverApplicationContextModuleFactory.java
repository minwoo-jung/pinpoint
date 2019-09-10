/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.context.module.config.ConfigModule;

/**
 * @author Taejin Koo
 */
public class NaverApplicationContextModuleFactory implements ModuleFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String GRPC_MODULE = "GRPC";
    public static final String THRIFT_MODULE = "THRIFT";

    @Override
    public Module newModule(AgentOption agentOption) {
        final Module config = new ConfigModule(agentOption);
        final Module pluginModule = new PluginModule();
        final Module applicationContextModule = new ApplicationContextModule();
        final Module rpcModule = newRpcModule(agentOption);
        final Module statsModule = new StatsModule();
        final Module thriftStatsModule = new ThriftStatsModule();

        return Modules.combine(config, pluginModule, applicationContextModule, rpcModule, statsModule, thriftStatsModule);
    }

    private Module newRpcModule(AgentOption agentOption) {
        ProfilerConfig profilerConfig = agentOption.getProfilerConfig();
        final String transportModule = profilerConfig.getTransportModule();
        if (GRPC_MODULE.equalsIgnoreCase(transportModule)) {
            logger.info("load GrpcModule");
            return new NaverSecurityGrpcModule(profilerConfig);
        }
        if (THRIFT_MODULE.equalsIgnoreCase(transportModule)) {
            logger.info("load ThriftModule");
            return new NaverRpcModule(profilerConfig);
        }
        logger.info("load ThriftModule");
        return new NaverRpcModule(profilerConfig);
    }

}

