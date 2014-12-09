package com.navercorp.pinpoint.profiler.modifier.bloc4;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;

public class RequestProcessorModifier  extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public RequestProcessorModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }
        
        try {
            InstrumentClass npcHandler = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);
            Interceptor messageReceivedInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.bloc4.interceptor.ProcessInterceptor");
            npcHandler.addInterceptor("process", new String[] {"com.nhncorp.lucy.bloc.core.processor.BlocRequest"}, messageReceivedInterceptor);

            return npcHandler.toBytecode();
        } catch (InstrumentException e) {
            logger.warn("RequestProcessorModifier fail. Caused:", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String getTargetClass() {
        return "com/nhncorp/lucy/bloc/core/processor/RequestProcessor";
    }
}
