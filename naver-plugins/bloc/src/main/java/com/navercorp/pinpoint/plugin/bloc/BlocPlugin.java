package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.plugin.bloc.v3.Bloc3Detector;
import com.navercorp.pinpoint.plugin.bloc.v4.Bloc4Detector;

public class BlocPlugin implements ProfilerPlugin, BlocConstants {
    
    @Override
    public void setup(ProfilerPluginContext context) {
        context.addApplicationTypeDetector(new Bloc3Detector(), new Bloc4Detector());
        
        addBlocAdapterEditor(context);
        addNettyInboundHandlerModifier(context);
        addNpcHandlerModifier(context);
        addRequestProcessorModifier(context);
        addModuleClassLoaderFactoryInterceptor(context);
    }

    private void addBlocAdapterEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.bloc.handler.HTTPHandler$BlocAdapter");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.bloc.v3.interceptor.ExecuteMethodInterceptor");
        context.addClassFileTransformer(builder.build());
    }
    
    private void addNettyInboundHandlerModifier(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.bloc.http.NettyInboundHandler");
        builder.injectFieldAccessor(FIELD_URI_ENCODING);
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ChannelRead0Interceptor");
        context.addClassFileTransformer(builder.build());
    }    
    
    private void addNpcHandlerModifier(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.bloc.npc.handler.NpcHandler");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.MessageReceivedInterceptor");
        context.addClassFileTransformer(builder.build());
    }

    private void addRequestProcessorModifier(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.bloc.core.processor.RequestProcessor");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ProcessInterceptor");
        context.addClassFileTransformer(builder.build());
    }
    
    private void addModuleClassLoaderFactoryInterceptor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.bloc.core.clazz.ModuleClassLoaderFactory");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ModuleClassLoaderFactoryInterceptor");
        context.addClassFileTransformer(builder.build());
    }
}
