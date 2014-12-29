package com.navercorp.pinpoint.profiler.modifier.nbase.arc;

import java.security.ProtectionDomain;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.nbase.arc.filter.NameBasedMethodFilter;
import com.navercorp.pinpoint.profiler.modifier.nbase.arc.filter.RedisClusterPipelineMethodNames;

/**
 * RedisCluster(nBase-ARC client) pipeline modifier
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterPipelineModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public RedisClusterPipelineModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "com/nhncorp/redis/cluster/pipeline/RedisClusterPipeline";
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", className);
        }

        try {
            final InstrumentClass instrumentClass = byteCodeInstrumentor.getClass(classLoader, className, classFileBuffer);

            // trace destinationId, endPoint
            instrumentClass.addTraceValue(MapTraceValue.class);

            addConstructorInterceptor(classLoader, protectedDomain, instrumentClass);
            addSetServerMethodInterceptor(classLoader, protectedDomain, instrumentClass);
            
            // method
            addMethodInterceptor(classLoader, protectedDomain, instrumentClass);

            return instrumentClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to modifier. caused={}", e.getMessage(), e);
            }
        }

        return null;
    }

    protected void addConstructorInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws InstrumentException {
        final Interceptor constructorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.nbase.arc.interceptor.RedisClusterPipelineConstructorInterceptor");
        try {
            instrumentClass.addConstructorInterceptor(new String[] { "com.nhncorp.redis.cluster.gateway.GatewayServer" }, constructorInterceptor);
        } catch (Exception e) {
            // backward compatibility error
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to add constructor interceptor. caused={}", e.getMessage(), e);
            }
        }
    }

    protected void addSetServerMethodInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws InstrumentException {
        final List<MethodInfo> declaredMethods = instrumentClass.getDeclaredMethods();
        for (MethodInfo method : declaredMethods) {
            if(!method.getName().equals("setServer")) {
                continue;
            }

            try {
                final Interceptor methodInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.nbase.arc.interceptor.RedisClusterPipelineSetServerMethodInterceptor");
                instrumentClass.addInterceptor(method.getName(), method.getParameterTypes(), methodInterceptor);
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to add 'setServer' method interceptor('not found ...' is jedis compatibility error). caused={}", e.getMessage(), e);
                }
            }
        }
    }
    
    protected void addMethodInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws InstrumentException {
        final List<MethodInfo> declaredMethods = instrumentClass.getDeclaredMethods(new NameBasedMethodFilter(RedisClusterPipelineMethodNames.get()));
        for (MethodInfo method : declaredMethods) {
            try {
                final Interceptor methodInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.nbase.arc.interceptor.RedisClusterPipelineMethodInterceptor");
                instrumentClass.addInterceptor(method.getName(), method.getParameterTypes(), methodInterceptor);
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to add method interceptor('not found ...' is jedis compatibility error). caused={}", e.getMessage(), e);
                }
            }
        }
    }
}