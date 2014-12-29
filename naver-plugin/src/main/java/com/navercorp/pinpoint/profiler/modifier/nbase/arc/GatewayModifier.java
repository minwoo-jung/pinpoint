package com.navercorp.pinpoint.profiler.modifier.nbase.arc;

import java.security.ProtectionDomain;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;

/**
 * Gateway(nBase-ARC client) modifier
 * - trace destinationId
 * 
 * @author jaehong.kim
 *
 */
public class GatewayModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public GatewayModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "com/nhncorp/redis/cluster/gateway/Gateway";
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", className);
        }

        try {
            final InstrumentClass instrumentClass = byteCodeInstrumentor.getClass(classLoader, className, classFileBuffer);

            // trace destinationId
            instrumentClass.addTraceValue(MapTraceValue.class);
            final Interceptor constructorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.nbase.arc.interceptor.GatewayConstructorInterceptor");
            instrumentClass.addConstructorInterceptor(new String[] { "com.nhncorp.redis.cluster.gateway.GatewayConfig" }, constructorInterceptor);

            // method
            final List<MethodInfo> declaredMethods = instrumentClass.getDeclaredMethods();
            for (MethodInfo method : declaredMethods) {
                if (method.getName().equals("getServer")) {
                    final Interceptor methodInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.nbase.arc.interceptor.GatewayMethodInterceptor");
                    instrumentClass.addInterceptor(method.getName(), method.getParameterTypes(), methodInterceptor);
                }
            }

            return instrumentClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to modifier. caused={}", e.getMessage(), e);
            }
        }

        return null;
    }
}