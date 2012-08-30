package com.profiler.modifier.db.mysql;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.ByteArrayClassPath;

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
		classPool.insertClassPath(new ByteArrayClassPath(javassistClassName, classFileBuffer));

		InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

		boolean instrumented = aClass.addInterceptor("executeQuery", new String[] { "java.lang.String" }, newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.ExecuteQueryMethodInterceptor"));

		instrumented &= aClass.addInterceptor("executeUpdate", new String[] { "java.lang.String", "boolean", "boolean" }, newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.ExecuteUpdateMethodInterceptor"));

		System.out.println("instrumented=" + instrumented);
		
		if (!instrumented) {
			return null;
		}

		return aClass.toBytecode();
	}
}