package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;
import com.navercorp.pinpoint.plugin.bloc.v3.Bloc3Detector;
import com.navercorp.pinpoint.plugin.bloc.v4.Bloc4Detector;

public class BlocPlugin implements ProfilerPlugin, BlocConstants {
    
    @Override
    public void setUp(ProfilerPluginSetupContext context) {
        context.addServerTypeDetector(new Bloc3Detector(), new Bloc4Detector());
        
        addBlocAdapterEditor(context);
        addNettyInboundHandlerModifier(context);
        addNpcHandlerModifier(context);
        addRequestProcessorModifier(context);
        addModuleClassLoaderFactoryInterceptor(context);
    }

    private void addBlocAdapterEditor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("com.nhncorp.lucy.bloc.handler.HTTPHandler$BlocAdapter");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.bloc.v3.interceptor.ExecuteMethodInterceptor");
        context.addClassEditor(builder.build());
    }
    
    private void addNettyInboundHandlerModifier(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("com.nhncorp.lucy.bloc.http.NettyInboundHandler");
        builder.injectFieldSnooper(FIELD_URI_ENCODING);
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ChannelRead0Interceptor");
        context.addClassEditor(builder.build());
    }    
    
    private void addNpcHandlerModifier(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("com.nhncorp.lucy.bloc.npc.handler.NpcHandler");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.MessageReceivedInterceptor");
        context.addClassEditor(builder.build());
    }

    private void addRequestProcessorModifier(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("com.nhncorp.lucy.bloc.core.processor.RequestProcessor");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ProcessInterceptor");
        context.addClassEditor(builder.build());
    }
    
    private void addModuleClassLoaderFactoryInterceptor(ProfilerPluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("com.nhncorp.lucy.bloc.core.clazz.ModuleClassLoaderFactory");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ModuleClassLoaderFactoryInterceptor");
    }
}
