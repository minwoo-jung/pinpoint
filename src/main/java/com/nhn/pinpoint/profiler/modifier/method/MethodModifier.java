package com.nhn.pinpoint.profiler.modifier.method;

import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.Method;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import javassist.CtClass;
import javassist.CtMethod;

import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * 
 */
public class MethodModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public MethodModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "*";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

		try {
			InstrumentClass clazz = byteCodeInstrumentor.getClass(javassistClassName);

			if (!clazz.isInterceptable()) {
				return null;
			}

			List<Method> methodList = clazz.getDeclaredMethods(EmptyMethodFilter.FILTER);
			for (Method method : methodList) {
				Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor");
                if (logger.isTraceEnabled()) {
                    logger.trace("### c=" + javassistClassName + ", m=" + method.getMethodName() + ", params=" + Arrays.toString(method.getMethodParams()));
                }
				clazz.addInterceptor(method.getMethodName(), method.getMethodParams(), interceptor);
			}

			return clazz.toBytecode();
		} catch (Exception e) {
			logger.warn("modify fail. Cause:" + e.getMessage(), e);
			return null;
		}
	}
}