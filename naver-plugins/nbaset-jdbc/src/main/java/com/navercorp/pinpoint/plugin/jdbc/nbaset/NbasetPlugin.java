/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.PreparedStatementBindingMethodFilter;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptorV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementCreateInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementCreateInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import com.navercorp.pinpoint.plugin.jdbc.nbaset.interceptor.NBaseDataSourceCreateMethodInterceptor;
import com.navercorp.pinpoint.plugin.jdbc.nbaset.interceptor.NBaseDataSourceGetConnectionMethodInterceptor;
import com.navercorp.pinpoint.plugin.jdbc.nbaset.interceptor.NbaseThreadLocalMethodInterceptor;

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author jaehong.kim
 */
public class NbasetPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final JdbcUrlParserV2 jdbcUrlParser = new NbasetJdbcUrlParser();

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        NbasetConfig config = new NbasetConfig(context.getConfig());
        if (!config.isPluginEnable()) {
            logger.info("Disable nbase-t jdbc option. 'profiler.jdbc.nbaset=false'");
            return;
        }

        context.addJdbcUrlParser(jdbcUrlParser);

        addConnectionTransformer(config);
        addDriverTransformer();
        addDataSourceTransformer();
        addPreparedStatementTransformer(config);
        addStatementTransformer();
        addThreadLocalTransformer();
    }

    private void addConnectionTransformer(final NbasetConfig config) {


        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.AbstractNbaseConnection", ConnectionTransform.class);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseConnection", ConnectionTransform.class);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllCkeyListConnection", ConnectionTransform.class);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllConnection", ConnectionTransform.class);

        // over 2.1.13
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.connection.AbstractNbaseConnection", ConnectionTransform.class);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.connection.NbaseDynamicConnection", ConnectionTransform.class);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.connection.NbaseStaticConnection", ConnectionTransform.class);
    }

    public static class ConnectionTransform implements TransformCallback {
        private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                logger.debug("## Not interceptable {}", className);
                return null;
            }
            NbasetConfig config = new NbasetConfig(instrumentor.getProfilerConfig());
            target.addField(DatabaseInfoAccessor.class);

            final MethodFilter connectionCloseMethodFilter = MethodFilters.chain(MethodFilters.name("close"), MethodFilters.modifierNot(Modifier.ABSTRACT));
            target.addScopedInterceptor(connectionCloseMethodFilter, ConnectionCloseInterceptor.class, NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);
            final MethodFilter statementCreateMethodFilter = MethodFilters.chain(MethodFilters.name("createStatement"), MethodFilters.modifierNot(Modifier.ABSTRACT));
            target.addScopedInterceptor(statementCreateMethodFilter, StatementCreateInterceptor.class, NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);
            final MethodFilter preparedStatementCreateMethodFilter = MethodFilters.chain(MethodFilters.name("prepareStatement", "prepareCall"), MethodFilters.modifierNot(Modifier.ABSTRACT));
            target.addScopedInterceptor(preparedStatementCreateMethodFilter, PreparedStatementCreateInterceptor.class, NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);

            if (config.isProfileSetAutoCommit()) {
                final MethodFilter methodFilter = MethodFilters.chain(MethodFilters.name("setAutoCommit"), MethodFilters.modifierNot(Modifier.ABSTRACT));
                target.addScopedInterceptor(methodFilter, TransactionSetAutoCommitInterceptor.class, NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            if (config.isProfileCommit()) {
                final MethodFilter methodFilter = MethodFilters.chain(MethodFilters.name("commit"), MethodFilters.modifierNot(Modifier.ABSTRACT));
                target.addScopedInterceptor(methodFilter, TransactionCommitInterceptor.class, NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            if (config.isProfileRollback()) {
                final MethodFilter methodFilter = MethodFilters.chain(MethodFilters.name("rollback"), MethodFilters.modifierNot(Modifier.ABSTRACT));
                target.addScopedInterceptor(methodFilter, TransactionRollbackInterceptor.class, NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }
    }

    ;

    private void addDriverTransformer() {
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseDriver", NbaseDriverTransform.class);
    }

    public static class NbaseDriverTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            InstrumentMethod connectMethod = InstrumentUtils.findMethod(target, "connect", "java.lang.String", "java.util.Properties");
            connectMethod.addScopedInterceptor(DriverConnectInterceptorV2.class, va(NbasetConstants.NBASET), NbasetConstants.NBASET_SCOPE, ExecutionPolicy.ALWAYS);

            return target.toBytecode();
        }
    }

    private void addDataSourceTransformer() {
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseDataSource", NbaseDataSourceTransform.class);


        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseNormalQueryDataSource", NbaseDataSourceExTransform.class);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllDataSource", NbaseDataSourceExTransform.class);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllCkeyListDataSource", NbaseDataSourceExTransform.class);

        // over 2.1.13
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.datasource.NbaseDynamicDataSource", NbaseDataSourceExTransform.class);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.datasource.NbaseStaticDataSource", NbaseDataSourceExTransform.class);
    }

    public static class NbaseDataSourceTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            InstrumentMethod method = target.getDeclaredMethod("create", "java.lang.String");
            if (method != null) {
                method.addScopedInterceptor(NBaseDataSourceCreateMethodInterceptor.class, NbasetConstants.NBASET_SCOPE, ExecutionPolicy.ALWAYS);
            }

            return target.toBytecode();
        }
    }

    public static class NbaseDataSourceExTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);
            InstrumentMethod method = target.getDeclaredMethod("getConnection");
            if (method != null) {
                method.addScopedInterceptor(NBaseDataSourceGetConnectionMethodInterceptor.class, NbasetConstants.NBASET_SCOPE, ExecutionPolicy.ALWAYS);
            }

            return target.toBytecode();
        }
    }

    ;

    private void addPreparedStatementTransformer(final NbasetConfig config) {


        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.AbstractNbasePreparedStatement", PreparedStatementTransform.class); // execute, executeQuery, executeUpdate
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbasePreparedStatement", PreparedStatementTransform.class); // none
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllCkeyListPreparedStatement", PreparedStatementTransform.class); // execute, executeQuery
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllPreparedStatement", PreparedStatementTransform.class); // none

        // over 2.1.13
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.statement.AbstractNbasePreparedStatement", PreparedStatementTransform.class);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.statement.NbaseDynamicPreparedStatement", PreparedStatementTransform.class);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.statement.NbaseStaticPreparedStatement", PreparedStatementTransform.class);
    }

    public static class PreparedStatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            NbasetConfig config = new NbasetConfig(instrumentor.getProfilerConfig());
            int maxBindValueSize = config.getMaxSqlBindValueSize();
            final MethodFilter preparedStatementExecuteQueryMethodFilter = MethodFilters.chain(MethodFilters.name("execute", "executeQuery", "executeUpdate"), MethodFilters.modifierNot(Modifier.ABSTRACT));
            target.addScopedInterceptor(preparedStatementExecuteQueryMethodFilter, PreparedStatementExecuteQueryInterceptor.class, va(maxBindValueSize), NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);

            if (config.isTraceSqlBindValue()) {
                PreparedStatementBindingMethodFilter methodFilter = new PreparedStatementBindingMethodFilter();
                target.addScopedInterceptor(methodFilter, PreparedStatementBindVariableInterceptor.class, NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }
    }

    ;

    private void addStatementTransformer() {
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.AbstractNbaseStatement", StatementTransform.class); // execute, executeQuery, executeUpdate
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseStatement", StatementTransform.class); // none
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllCkeyListStatement", StatementTransform.class); // execute, execueQuery,
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllStatement", StatementTransform.class); // none

        // over 2.1.13
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.statement.AbstractNbaseStatement", StatementTransform.class);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.statement.NbaseDynamicStatement", StatementTransform.class);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.statement.NbaseStaticStatement", StatementTransform.class);
    }

    public static class StatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            final MethodFilter statementExecuteQueryMethodFilter = MethodFilters.chain(MethodFilters.name("executeQuery"), MethodFilters.modifierNot(Modifier.ABSTRACT));
            target.addScopedInterceptor(statementExecuteQueryMethodFilter, StatementExecuteQueryInterceptor.class, NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);
            final MethodFilter statementExecuteUpdateMethodFilter = MethodFilters.chain(MethodFilters.name("executeUpdate", "execute"), MethodFilters.modifierNot(Modifier.ABSTRACT));
            target.addScopedInterceptor(statementExecuteUpdateMethodFilter, StatementExecuteUpdateInterceptor.class, NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);

            return target.toBytecode();
        }
    }

    ;

    private void addThreadLocalTransformer() {
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseThreadLocal", NbaseThreadLocalTransform.class);
    }

    public static class NbaseThreadLocalTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.getDeclaredMethod("setCkey", "java.lang.String").addInterceptor(NbaseThreadLocalMethodInterceptor.class);

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}