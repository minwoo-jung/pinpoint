package com.navercorp.pinpoint.plugin.bloc.v4;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;

import java.security.ProtectionDomain;

public class Bloc4Plugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        context.addApplicationTypeDetector(new Bloc4Detector());
        
        addNettyInboundHandlerModifier(context);
        addNpcHandlerModifier(context);
        addNimmHandlerModifider(context);
        addRequestProcessorModifier(context);
        addModuleClassLoaderFactoryInterceptor(context);
    }

    private void addNettyInboundHandlerModifier(ProfilerPluginSetupContext context) {
        transformTemplate.transform("com.nhncorp.lucy.bloc.http.NettyInboundHandler", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addGetter("com.navercorp.pinpoint.plugin.bloc.v4.UriEncodingGetter", "uriEncoding");
                target.addInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ChannelRead0Interceptor");
                return target.toBytecode();
            }
        });
    }    
    
    private void addNpcHandlerModifier(ProfilerPluginSetupContext context) {
        transformTemplate.transform("com.nhncorp.lucy.bloc.npc.handler.NpcHandler", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.MessageReceivedInterceptor");
                return target.toBytecode();
            }
        });
    }

    private void addNimmHandlerModifider(ProfilerPluginSetupContext context) {
        transformTemplate.transform("com.nhncorp.lucy.bloc.nimm.handler.NimmHandler$ContextWorker", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(BlocConstants.NIMM_SERVER_SOCKET_ADDRESS_ACCESSOR);

                InstrumentMethod setPrefableNetEndPoint = target.addDelegatorMethod("setPrefableNetEndPoint", "com.nhncorp.lucy.nimm.connector.NimmNetEndPoint");
                if (setPrefableNetEndPoint != null) {
                    setPrefableNetEndPoint.addInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.NimmAbstractWorkerInterceptor");
                }

                target.addInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.NimmHandlerInterceptor");
                return target.toBytecode();
            }
        });
    }

    private void addRequestProcessorModifier(ProfilerPluginSetupContext context) {
        transformTemplate.transform("com.nhncorp.lucy.bloc.core.processor.RequestProcessor", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ProcessInterceptor");
                return target.toBytecode();
            }
        });
    }
    
    private void addModuleClassLoaderFactoryInterceptor(ProfilerPluginSetupContext context) {
        transformTemplate.transform("com.nhncorp.lucy.bloc.core.clazz.ModuleClassLoaderFactory", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ModuleClassLoaderFactoryInterceptor");
                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
