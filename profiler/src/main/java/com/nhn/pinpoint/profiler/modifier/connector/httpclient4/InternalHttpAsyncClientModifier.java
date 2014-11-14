package com.nhn.pinpoint.profiler.modifier.connector.httpclient4;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;

/**
 * 
 * @author netspider
 * 
 */
@Deprecated
public class InternalHttpAsyncClientModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public InternalHttpAsyncClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	@Override
	public String getTargetClass() {
		return "org/apache/http/impl/nio/client/InternalHttpAsyncClient";
	}

	@Override
	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {} @ {}", javassistClassName, classLoader);
		}

		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

			Interceptor internalExecuteInterceptor = byteCodeInstrumentor.newInterceptor(classLoader,
					protectedDomain,
					"com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor.AsyncInternalClientExecuteInterceptor");
			
			String[] internalExecuteParams = new String[] { 
					"org.apache.http.nio.protocol.HttpAsyncRequestProducer", 
					"org.apache.http.nio.protocol.HttpAsyncResponseConsumer", 
					"org.apache.http.protocol.HttpContext", 
					"org.apache.http.concurrent.FutureCallback"
					};
			aClass.addInterceptor("execute", internalExecuteParams, internalExecuteInterceptor);
			
			return aClass.toBytecode();
		} catch (InstrumentException e) {
			logger.info("modify fail. Cause:{}", e.getMessage(), e);
			return null;
		}
	}
}
