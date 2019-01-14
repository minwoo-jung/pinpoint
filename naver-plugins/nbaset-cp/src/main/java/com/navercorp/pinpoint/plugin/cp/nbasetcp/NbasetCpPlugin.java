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
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import com.navercorp.pinpoint.plugin.cp.nbasetcp.filed.getter.MaxConnPoolSizeGetter;
import com.navercorp.pinpoint.plugin.cp.nbasetcp.filed.getter.NumUsedSocketsGetter;
import com.navercorp.pinpoint.plugin.cp.nbasetcp.interceptor.CloseConnectionInterceptor;
import com.navercorp.pinpoint.plugin.cp.nbasetcp.interceptor.CloseInterceptor;
import com.navercorp.pinpoint.plugin.cp.nbasetcp.interceptor.ConstructorInterceptor;
import com.navercorp.pinpoint.plugin.cp.nbasetcp.interceptor.GetConnectionInterceptor;
import com.navercorp.pinpoint.plugin.cp.nbasetcp.interceptor.ReturnConnectionInterceptor;

import java.security.ProtectionDomain;

/**
 * @author Taejin Koo
 */
public class NbasetCpPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        NbasetCpConfig config = new NbasetCpConfig(context.getConfig());

        if (!config.isPluginEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        context.addJdbcUrlParser(new NbasetCpUrlParser());
        addConnectionPoolTransformer();
    }

    private void addConnectionPoolTransformer() {
        transformTemplate.transform("com.nhncorp.nbase_t.krpc.core.ConnectionPool", ConnectionPoolTransform.class);
    }

    public static class ConnectionPoolTransform implements TransformCallback {
        private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

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

            target.addField(NbasetCpMonitorAccessor.class);
            // constructor
            constructor.addScopedInterceptor(ConstructorInterceptor.class, NbasetCpConstants.SCOPE);

            target.addGetter(NumUsedSocketsGetter.class, NbasetCpConstants.FIELD_NUM_USED_SOCKET);
            target.addGetter(MaxConnPoolSizeGetter.class, NbasetCpConstants.FIELD_MAX_POOL_SIZE);

            // getConnection method
            InstrumentMethod getConnectionMethod = InstrumentUtils.findMethod(target, "getConnection", "com.nhncorp.nbase_t.krpc.core.KrpcTarget");
            if (getConnectionMethod == null) {
                logger.info("can't find getConnection(com.nhncorp.nbase_t.krpc.core.KrpcTarget) method.");
            } else {
                getConnectionMethod.addScopedInterceptor(GetConnectionInterceptor.class, NbasetCpConstants.SCOPE);
            }

            // closeConnection
            InstrumentMethod closeConnectionMethod = InstrumentUtils.findMethod(target, "closeConnection", "java.net.Socket");
            if (closeConnectionMethod == null) {
                logger.info("can't find closeConnection(java.net.Socket) method.");
            } else {
                closeConnectionMethod.addScopedInterceptor(CloseConnectionInterceptor.class, NbasetCpConstants.SCOPE);
            }

            // returnConnection
            InstrumentMethod returnConnectionMethod = InstrumentUtils.findMethod(target, "returnConnection", "com.nhncorp.nbase_t.krpc.core.KrpcTarget", "java.net.Socket");
            if (returnConnectionMethod == null) {
                logger.info("can't find returnConnection(com.nhncorp.nbase_t.krpc.core.KrpcTarget, java.net.Socket) method.");
            } else {
                returnConnectionMethod.addScopedInterceptor(ReturnConnectionInterceptor.class, NbasetCpConstants.SCOPE);
            }

            // closeMethod
            InstrumentMethod closeMethod = InstrumentUtils.findMethod(target, "close");
            if (closeMethod == null) {
                logger.info("can't find close() method.");
            } else {
                closeMethod.addScopedInterceptor(CloseInterceptor.class, NbasetCpConstants.SCOPE);
            }

            return target.toBytecode();
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

    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
