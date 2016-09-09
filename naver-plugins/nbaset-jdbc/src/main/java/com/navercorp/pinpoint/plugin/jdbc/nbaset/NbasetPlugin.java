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

import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.PreparedStatementBindingMethodFilter;

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author jaehong.kim
 */
public class NbasetPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        NbasetConfig config = new NbasetConfig(context.getConfig());
        if(!config.isPluginEnable()) {
            logger.info("Disable nbase-t jdbc option. 'profiler.jdbc.nbaset=false'");
            return;
        }

        addConnectionTransformer(config);
        addDriverTransformer();
        addDataSourceTransformer();
        addPreparedStatementTransformer(config);
        addStatementTransformer();
        addThreadLocalTransformer();
    }

    private void addConnectionTransformer(final NbasetConfig config) {
        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    logger.debug("## Not interceptable {}", className);
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                final MethodFilter connectionCloseMethodFilter = MethodFilters.chain(MethodFilters.name("close"), MethodFilters.modifierNot(Modifier.ABSTRACT));
                target.addScopedInterceptor(connectionCloseMethodFilter, "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor", NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);
                final MethodFilter statementCreateMethodFilter = MethodFilters.chain(MethodFilters.name("createStatement"), MethodFilters.modifierNot(Modifier.ABSTRACT));
                target.addScopedInterceptor(statementCreateMethodFilter, "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementCreateInterceptor", NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);
                final MethodFilter preparedStatementCreateMethodFilter = MethodFilters.chain(MethodFilters.name("prepareStatement", "prepareCall"), MethodFilters.modifierNot(Modifier.ABSTRACT));
                target.addScopedInterceptor(preparedStatementCreateMethodFilter, "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementCreateInterceptor", NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);

                if (config.isProfileSetAutoCommit()) {
                    final MethodFilter methodFilter = MethodFilters.chain(MethodFilters.name("setAutoCommit"), MethodFilters.modifierNot(Modifier.ABSTRACT));
                    target.addScopedInterceptor(methodFilter, "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor", NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);
                }

                if (config.isProfileCommit()) {
                    final MethodFilter methodFilter = MethodFilters.chain(MethodFilters.name("commit"), MethodFilters.modifierNot(Modifier.ABSTRACT));
                    target.addScopedInterceptor(methodFilter, "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor", NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);
                }

                if (config.isProfileRollback()) {
                    final MethodFilter methodFilter = MethodFilters.chain(MethodFilters.name("rollback"), MethodFilters.modifierNot(Modifier.ABSTRACT));
                    target.addScopedInterceptor(methodFilter, "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor", NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);
                }

                return target.toBytecode();
            }
        };

        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.AbstractNbaseConnection", transformer);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseConnection", transformer);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllCkeyListConnection", transformer);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllConnection", transformer);

        // over 2.1.13
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.connection.AbstractNbaseConnection", transformer);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.connection.NbaseDynamicConnection", transformer);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.connection.NbaseStaticConnection", transformer);
    }

    private void addDriverTransformer() {
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseDriver", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptor", va(new NbasetJdbcUrlParser()), NbasetConstants.NBASET_SCOPE, ExecutionPolicy.ALWAYS);

                return target.toBytecode();
            }
        });
    }

    private void addDataSourceTransformer() {
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseDataSource", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                InstrumentMethod method = target.getDeclaredMethod("create", "java.lang.String");
                if (method != null) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.jdbc.nbaset.interceptor.NBaseDataSourceCreateMethodInterceptor", NbasetConstants.NBASET_SCOPE, ExecutionPolicy.ALWAYS);
                }

                return target.toBytecode();
            }
        });

        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                InstrumentMethod method = target.getDeclaredMethod("getConnection");
                if(method != null) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.jdbc.nbaset.interceptor.NBaseDataSourceGetConnectionMethodInterceptor", NbasetConstants.NBASET_SCOPE, ExecutionPolicy.ALWAYS);
                }

                return target.toBytecode();
            }
        };

        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseNormalQueryDataSource", transformer);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllDataSource", transformer);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllCkeyListDataSource", transformer);

        // over 2.1.13
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.datasource.NbaseDynamicDataSource", transformer);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.datasource.NbaseStaticDataSource", transformer);
    }

    private void addPreparedStatementTransformer(final NbasetConfig config) {
        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                int maxBindValueSize = config.getMaxSqlBindValueSize();
                final MethodFilter preparedStatementExecuteQueryMethodFilter = MethodFilters.chain(MethodFilters.name("execute", "executeQuery", "executeUpdate"), MethodFilters.modifierNot(Modifier.ABSTRACT));
                target.addScopedInterceptor(preparedStatementExecuteQueryMethodFilter, "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor", va(maxBindValueSize), NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);

                if (config.isTraceSqlBindValue()) {
                    PreparedStatementBindingMethodFilter methodFilter = new PreparedStatementBindingMethodFilter();
                    target.addScopedInterceptor(methodFilter, "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor", NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);
                }

                return target.toBytecode();
            }
        };

        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.AbstractNbasePreparedStatement", transformer); // execute, executeQuery, executeUpdate
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbasePreparedStatement", transformer); // none
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllCkeyListPreparedStatement", transformer); // execute, executeQuery
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllPreparedStatement", transformer); // none

        // over 2.1.13
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.statement.AbstractNbasePreparedStatement", transformer);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.statement.NbaseDynamicPreparedStatement", transformer);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.statement.NbaseStaticPreparedStatement", transformer);
    }

    private void addStatementTransformer() {
        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                final MethodFilter statementExecuteQueryMethodFilter = MethodFilters.chain(MethodFilters.name("executeQuery"), MethodFilters.modifierNot(Modifier.ABSTRACT));
                target.addScopedInterceptor(statementExecuteQueryMethodFilter, "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor", NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);
                final MethodFilter statementExecuteUpdateMethodFilter = MethodFilters.chain(MethodFilters.name("executeUpdate", "execute"), MethodFilters.modifierNot(Modifier.ABSTRACT));
                target.addScopedInterceptor(statementExecuteUpdateMethodFilter, "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor", NbasetConstants.NBASET_SCOPE, ExecutionPolicy.BOUNDARY);

                return target.toBytecode();
            }
        };

        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.AbstractNbaseStatement", transformer); // execute, executeQuery, executeUpdate
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseStatement", transformer); // none
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllCkeyListStatement", transformer); // execute, execueQuery,
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseQueryAllStatement", transformer); // none

        // over 2.1.13
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.statement.AbstractNbaseStatement", transformer);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.statement.NbaseDynamicStatement", transformer);
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.statement.NbaseStaticStatement", transformer);
    }

    private void addThreadLocalTransformer() {
        transformTemplate.transform("com.nhncorp.nbase_t.jdbc.NbaseThreadLocal", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.getDeclaredMethod("setCkey", "java.lang.String").addInterceptor("com.navercorp.pinpoint.plugin.jdbc.nbaset.interceptor.NbaseThreadLocalMethodInterceptor");

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}