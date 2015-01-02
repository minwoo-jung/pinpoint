package com.navercorp.pinpoint.profiler.modifier.connector.npc;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.npc.interceptor.InvokeInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NPC Hessian connector modifier
 * 
 * based on NPC client 1.5.18
 * 
 * @author netspider
 */
public class LightWeightNpcHessianConnectorModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public LightWeightNpcHessianConnectorModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "com/nhncorp/lucy/npc/connector/LightWeightNpcHessianConnector";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }

        try {
            InstrumentClass connectorClass = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

            // trace variables
            connectorClass.addTraceVariable("_serverAddress", "__setServerAddress", "__getServerAddress", "java.net.InetSocketAddress");

            // constructor
            Interceptor constructorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.connector.npc.interceptor.ConnectorConstructorInterceptor");
            connectorClass.addConstructorInterceptor(new String[] { "com.nhncorp.lucy.npc.connector.NpcConnectorOption" }, constructorInterceptor);

            // invoke
            Interceptor invokeInterceptor = new InvokeInterceptor();
            connectorClass.addInterceptor("invoke", new String[] { "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]" }, invokeInterceptor);

            return connectorClass.toBytecode();
        } catch (Throwable e) {
            logger.warn(this.getClass().getSimpleName() + " modifier error. Caused:{}", e.getMessage(), e);
            return null;
        }
    }
}