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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;

import java.security.ProtectionDomain;

/**
 * @author Taejin Koo
 */
public class NbasetCpPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final JdbcUrlParserV2 parser = new NbasetCpUrlParser();

    private NbasetCpConfig config;
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        config = new NbasetCpConfig(context.getConfig());
        if (!config.isPluginEnable()) {
            logger.info("Disable nbasetcp option. 'profiler.jdbc.nbaset.cp=false'");
            return;
        }

        context.addJdbcUrlParser(parser);
        addConnectionPoolTransformer();
    }

    private void addConnectionPoolTransformer() {
        transformTemplate.transform("com.nhncorp.nbase_t.krpc.core.ConnectionPool", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                if (!isAvailableDataSourceMonitor(target)) {
                    return target.toBytecode();
                }

                InstrumentMethod constructor = target.getConstructor();
                if (constructor == null) {
                    logger.info("can't find ConnectionPool() constructor.");
                    return target.toBytecode();
                }

                target.addField(NbasetCpConstants.ACCESSOR_CP_MONITOR);
                // constructor
                constructor.addScopedInterceptor(NbasetCpConstants.INTERCEPTOR_CONSTRUCTOR, NbasetCpConstants.SCOPE);

                target.addGetter(NbasetCpConstants.GETTER_NUM_USED_SOCKET, NbasetCpConstants.FIELD_NUM_USED_SOCKET);
                target.addGetter(NbasetCpConstants.GETTER_MAX_POOL_SIZE, NbasetCpConstants.FIELD_MAX_POOL_SIZE);

                // getConnection method
                InstrumentMethod getConnectionMethod = InstrumentUtils.findMethod(target, "getConnection", "com.nhncorp.nbase_t.krpc.core.KrpcTarget");
                if (getConnectionMethod == null) {
                    logger.info("can't find getConnection(com.nhncorp.nbase_t.krpc.core.KrpcTarget) method.");
                } else {
                    getConnectionMethod.addScopedInterceptor(NbasetCpConstants.INTERCEPTOR_GET_CONNECTION, NbasetCpConstants.SCOPE);
                }

                // closeConnection
                InstrumentMethod closeConnectionMethod = InstrumentUtils.findMethod(target, "closeConnection", "java.net.Socket");
                if (closeConnectionMethod == null) {
                    logger.info("can't find closeConnection(java.net.Socket) method.");
                } else {
                    closeConnectionMethod.addScopedInterceptor(NbasetCpConstants.INTERCEPTOR_CLOSE_CONNECTION, NbasetCpConstants.SCOPE);
                }

                // returnConnection
                InstrumentMethod returnConnectionMethod = InstrumentUtils.findMethod(target, "returnConnection", "com.nhncorp.nbase_t.krpc.core.KrpcTarget", "java.net.Socket");
                if (returnConnectionMethod == null) {
                    logger.info("can't find returnConnection(com.nhncorp.nbase_t.krpc.core.KrpcTarget, java.net.Socket) method.");
                } else {
                    returnConnectionMethod.addScopedInterceptor(NbasetCpConstants.INTERCEPTOR_RETURN_CONNECTION, NbasetCpConstants.SCOPE);
                }

                // closeMethod
                InstrumentMethod closeMethod = InstrumentUtils.findMethod(target, "close");
                if (closeMethod == null) {
                    logger.info("can't find close() method.");
                } else {
                    closeMethod.addScopedInterceptor(NbasetCpConstants.INTERCEPTOR_CLOSE, NbasetCpConstants.SCOPE);
                }

                return target.toBytecode();
            }

        });
    }

    private boolean isAvailableDataSourceMonitor(InstrumentClass target) {
        if (!target.hasField(NbasetCpConstants.FIELD_NUM_USED_SOCKET, "java.util.concurrent.atomic.AtomicInteger")) {
            return false;
        }
        if (!target.hasField(NbasetCpConstants.FIELD_MAX_POOL_SIZE, "java.util.concurrent.atomic.AtomicInteger")) {
            return false;
        }
        return true;
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
