package com.profiler.modifier.tomcat;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.Agent;
import com.profiler.LifeCycleEventListener;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.modifier.tomcat.interceptors.StandardServiceStartInterceptor;
import com.profiler.modifier.tomcat.interceptors.StandardServiceStopInterceptor;

import com.profiler.modifier.AbstractModifier;

/**
 * When org.apache.catalina.core.StandardService class is loaded in ClassLoader,
 * this class modifies methods.
 *
 * @author cowboy93, netspider
 */
public class TomcatStandardServiceModifier extends AbstractModifier {

    private final Logger logger = Logger.getLogger(TomcatStandardServiceModifier.class.getName());


    public TomcatStandardServiceModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/catalina/core/StandardService";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Modifing. " + javassistClassName);
        }
        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

        try {
            InstrumentClass standardService = byteCodeInstrumentor.getClass(javassistClassName);

            LifeCycleEventListener lifeCycleEventListener = new LifeCycleEventListener(agent);
            StandardServiceStartInterceptor start = new StandardServiceStartInterceptor(lifeCycleEventListener);
            standardService.addInterceptor("start", null, start);

            StandardServiceStopInterceptor stop = new StandardServiceStopInterceptor(lifeCycleEventListener);
            standardService.addInterceptor("stop", null, stop);

            return standardService.toBytecode();
        } catch (InstrumentException e) {
            logger.log(Level.WARNING, "modify fail. Cause:" + e.getMessage(), e);
            return null;
        }
    }
}
