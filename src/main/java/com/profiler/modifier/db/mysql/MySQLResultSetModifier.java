package com.profiler.modifier.db.mysql;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class MySQLResultSetModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(MySQLResultSetModifier.class);

	public static byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("ResultSetImpl modifing. %s", javassistClassName);
		return changeMethod(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private static byte[] changeMethod(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			updateNextMethod(classPool, cc);
			updateCloseMethod(classPool, cc);
			byte[] newClassfileBuffer = cc.toBytecode();
			// cc.writeFile();
			printClassConvertComplete(javassistClassName);
			return newClassfileBuffer;
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private static void updateNextMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod serviceMethod1 = cc.getDeclaredMethod("next", null);

		logger.info("*** Changing next() method");
		
		serviceMethod1.insertBefore(getNextMethodBeforeInsertCode());
	}

	private static String getNextMethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".updateFetchCount();");
		sb.append("}");
		return sb.toString();
	}

	private static void updateCloseMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod serviceMethod1 = cc.getDeclaredMethod("close", null);

		logger.info("Changing close() method");

		serviceMethod1.insertBefore(getCloseMethodBeforeInsertCode());
	}

	private static String getCloseMethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".addResultSetData();");
		sb.append("}");

		return sb.toString();
	}
}
