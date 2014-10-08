package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.DedicatedModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.DriverConnectInterceptor;
import com.nhn.pinpoint.profiler.util.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CubridDriverModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public CubridDriverModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "cubrid/jdbc/driver/CUBRIDDriver";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}
		this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass mysqlConnection = byteCodeInstrumentor.getClass(javassistClassName);

            final Scope scope = byteCodeInstrumentor.getScope(CubridScope.SCOPE_NAME);
            DriverConnectInterceptor driverConnectInterceptor = new DriverConnectInterceptor(scope);
            mysqlConnection.addInterceptor("connect", new String[]{"java.lang.String", "java.util.Properties"}, driverConnectInterceptor);

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
