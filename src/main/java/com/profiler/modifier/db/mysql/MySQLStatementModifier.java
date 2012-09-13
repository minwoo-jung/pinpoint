package com.profiler.modifier.db.mysql;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.modifier.db.interceptor.StatementExecuteQueryInterceptor;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;

public class MySQLStatementModifier extends AbstractModifier {

    private final Logger logger = Logger.getLogger(MySQLStatementModifier.class.getName());

    public MySQLStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
        super(byteCodeInstrumentor);
    }

    public String getTargetClass() {
        return "com/mysql/jdbc/StatementImpl";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Modifing. " + javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

        try {
            InstrumentClass statementClass = byteCodeInstrumentor.getClass(javassistClassName);
//            Interceptor interceptor = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.interceptor.StatementExecuteQueryInterceptor");
            Interceptor interceptor = new StatementExecuteQueryInterceptor();
            statementClass.addInterceptor("executeQuery", new String[]{"java.lang.String"}, interceptor);


            Interceptor interceptor1 = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.interceptor.ExecuteUpdateMethodInterceptor");
            statementClass.addInterceptor("executeUpdate", new String[]{"java.lang.String", "boolean", "boolean"}, interceptor1);

            statementClass.addTraceVariable("__url", "__setUrl", "__getUrl", "java.lang.String");
            return statementClass.toBytecode();
        } catch (InstrumentException e) {
            return null;
        }
    }


}