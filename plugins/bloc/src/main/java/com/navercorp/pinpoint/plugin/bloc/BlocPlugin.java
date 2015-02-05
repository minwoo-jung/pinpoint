package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.bootstrap.plugin.PluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;
import com.navercorp.pinpoint.common.plugin.ServiceTypeSetupContext;
import com.navercorp.pinpoint.plugin.bloc.v3.Bloc3Detector;
import com.navercorp.pinpoint.plugin.bloc.v4.Bloc4Detector;

public class BlocPlugin implements ProfilerPlugin, BlocConstants {
    
    @Override
    public void setUp(ServiceTypeSetupContext context) {
        context.addServiceType(BLOC, BLOC_INTERNAL_METHOD);
        context.addAnnotationKey(CALL_URL, CALL_PARAM, PROTOCOL);
    }

    @Override
    public void setUp(PluginSetupContext context) {
        context.addServerTypeDetector(new Bloc3Detector(), new Bloc4Detector());
        
        addBlocAdapterEditor(context);
        addNettyInboundHandlerModifier(context);
        addNpcHandlerModifier(context);
        addRequestProcessorModifier(context);
    }
    
    private void addBlocAdapterEditor(PluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("com.nhncorp.lucy.bloc.handler.HTTPHandler$BlocAdapter");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.bloc.v3.interceptor.ExecuteMethodInterceptor");
    }
    
    private void addNettyInboundHandlerModifier(PluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("com.nhncorp.lucy.bloc.http.NettyInboundHandler");
        builder.injectFieldSnooper(FIELD_URI_ENCODING);
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ChannelRead0Interceptor");
    }    
    
    private void addNpcHandlerModifier(PluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("com.nhncorp.lucy.bloc.npc.handler.NpcHandler");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.MessageReceivedInterceptor");
    }

    private void addRequestProcessorModifier(PluginSetupContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("com.nhncorp.lucy.bloc.core.processor.RequestProcessor");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ProcessInterceptor");
    }
}