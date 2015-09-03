package com.navercorp.pinpoint.plugin.bloc.v4;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginInstrumentContext;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;

public class Bloc4Plugin implements ProfilerPlugin, BlocConstants {
    
    @Override
    public void setup(ProfilerPluginSetupContext context) {
        context.addApplicationTypeDetector(new Bloc4Detector());
        
        addNettyInboundHandlerModifier(context);
        addNpcHandlerModifier(context);
        addRequestProcessorModifier(context);
        addModuleClassLoaderFactoryInterceptor(context);
    }

    private void addNettyInboundHandlerModifier(ProfilerPluginSetupContext context) {
        context.addClassFileTransformer("com.nhncorp.lucy.bloc.http.NettyInboundHandler", new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                
                target.addGetter("com.navercorp.pinpoint.plugin.bloc.v4.UriEncodingGetter", "uriEncoding");
                target.addInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ChannelRead0Interceptor");
                return target.toBytecode();
            }
        });
    }    
    
    private void addNpcHandlerModifier(ProfilerPluginSetupContext context) {
        context.addClassFileTransformer("com.nhncorp.lucy.bloc.npc.handler.NpcHandler", new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.MessageReceivedInterceptor");
                return target.toBytecode();
            }
        });
    }

    private void addRequestProcessorModifier(ProfilerPluginSetupContext context) {
        context.addClassFileTransformer("com.nhncorp.lucy.bloc.core.processor.RequestProcessor", new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ProcessInterceptor");
                return target.toBytecode();
            }
        });
    }
    
    private void addModuleClassLoaderFactoryInterceptor(ProfilerPluginSetupContext context) {
        context.addClassFileTransformer("com.nhncorp.lucy.bloc.core.clazz.ModuleClassLoaderFactory", new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ModuleClassLoaderFactoryInterceptor");
                return target.toBytecode();
            }
        });
    }
}
