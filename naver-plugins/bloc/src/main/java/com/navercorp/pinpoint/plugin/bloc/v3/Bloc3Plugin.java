package com.navercorp.pinpoint.plugin.bloc.v3;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;

public class Bloc3Plugin implements ProfilerPlugin, BlocConstants {
    
    @Override
    public void setup(ProfilerPluginSetupContext context) {
        context.addApplicationTypeDetector(new Bloc3Detector());
        
        addBlocAdapterEditor(context);
    }

    private void addBlocAdapterEditor(ProfilerPluginSetupContext context) {
        context.addClassFileTransformer("com.nhncorp.lucy.bloc.handler.HTTPHandler$BlocAdapter", new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addInterceptor("com.navercorp.pinpoint.plugin.bloc.v3.interceptor.ExecuteMethodInterceptor");
                return target.toBytecode();
            }
        });
    }
    
}
