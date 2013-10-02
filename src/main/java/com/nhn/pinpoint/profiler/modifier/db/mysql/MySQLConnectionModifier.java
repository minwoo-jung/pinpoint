package com.nhn.pinpoint.profiler.modifier.db.mysql;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.ScopeDelegateSimpleInterceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.interceptor.bci.Type;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;

/**
 *
 */
public class MySQLConnectionModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MySQLConnectionModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        // mysql의 과거버전의 경우 Connection class에 직접 구현이 되어있다.
        return "com/mysql/jdbc/Connection";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }
        this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        try {
            InstrumentClass mysqlConnection = byteCodeInstrumentor.getClass(javassistClassName);
            if (mysqlConnection.isInterface()) {
                // 최신버전의 mysql dirver를 사용했을 경우의 호환성 작업.
                return null;
            }


            mysqlConnection.addTraceVariable("__databaseInfo", "__setDatabaseInfo", "__getDatabaseInfo", "java.lang.Object");

            // 해당 Interceptor를 공통클래스 만들경우 system에 로드해야 된다.
//            Interceptor createConnection  = new ConnectionCreateInterceptor();
//            String[] params = new String[] {
//                "java.lang.String", "int", "java.util.Properties", "java.lang.String", "java.lang.String"
//            };
//            mysqlConnection.addInterceptor("getInstance", params, createConnection);


            Interceptor closeConnection = new ScopeDelegateSimpleInterceptor(new ConnectionCloseInterceptor(), JDBCScope.SCOPE);
            mysqlConnection.addInterceptor("close", null, closeConnection);

            Interceptor createStatement = new ScopeDelegateSimpleInterceptor(new StatementCreateInterceptor(), JDBCScope.SCOPE);
            mysqlConnection.addInterceptor("createStatement", null, createStatement);


            Interceptor preparedStatement = new ScopeDelegateSimpleInterceptor(new PreparedStatementCreateInterceptor(), JDBCScope.SCOPE);
            mysqlConnection.addInterceptor("prepareStatement", new String[]{"java.lang.String"}, preparedStatement);

            final ProfilerConfig profilerConfig = agent.getProfilerConfig();
            if (profilerConfig.isJdbcProfileMySqlSetAutoCommit()) {
                Interceptor setAutocommit = new ScopeDelegateSimpleInterceptor(new TransactionSetAutoCommitInterceptor(), JDBCScope.SCOPE);
                mysqlConnection.addInterceptor("setAutoCommit", new String[]{"boolean"}, setAutocommit);
            }
            if (profilerConfig.isJdbcProfileMySqlCommit()) {
                Interceptor commit = new ScopeDelegateSimpleInterceptor(new TransactionCommitInterceptor(), JDBCScope.SCOPE);
                mysqlConnection.addInterceptor("commit", null, commit);
            }
            if (profilerConfig.isJdbcProfileMySqlRollback()) {
                Interceptor rollback = new ScopeDelegateSimpleInterceptor(new TransactionRollbackInterceptor(), JDBCScope.SCOPE);
                mysqlConnection.addInterceptor("rollback", null, rollback);
            }

            if (this.logger.isInfoEnabled()) {
                this.logger.info("{} class is converted.", javassistClassName);
            }

            return mysqlConnection.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }
}

