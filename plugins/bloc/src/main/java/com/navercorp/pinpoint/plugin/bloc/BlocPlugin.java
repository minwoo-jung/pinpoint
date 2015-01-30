package com.navercorp.pinpoint.plugin.bloc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.plugin.ApplicationServerProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.FieldSnooper;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditor;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder.MethodEditorBuilder;
import com.navercorp.pinpoint.common.ServiceType;

public class BlocPlugin implements ApplicationServerProfilerPlugin {
    
    public String bloc4Home;
    public String bloc3Home;
    
    @Override
    public List<ClassEditor> getClassEditors(ProfilerPluginContext context) {
        List<ClassEditor> editors = new ArrayList<ClassEditor>();

        if (isBloc3()) {
            editors.add(getBlocAdapterEditor(context));
        } else if (isBloc4()) {
            editors.add(getNettyInboundHandlerModifier(context));
            editors.add(getNpcHandlerModifier(context));
            editors.add(getRequestProcessorModifier(context));
        }
        
        return editors;
    }
    
    private ClassEditor getBlocAdapterEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("com.nhncorp.lucy.bloc.handler.HTTPHandler$BlocAdapter");
        
        MethodEditorBuilder mb = builder.editMethod();
        mb.targetMethod("execute", "external.org.apache.coyote.Request", "external.org.apache.coyote.Response");
        mb.injectInterceptor().interceptorClass("com.navercorp.pinpoint.plugin.bloc.v3.interceptor.ExecuteMethodInterceptor");
        
        return builder.build();
    }
    
    private ClassEditor getNettyInboundHandlerModifier(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("com.nhncorp.lucy.bloc.http.NettyInboundHandler");
        builder.inject(FieldSnooper.OBJECT, "uriEncoding");
        
        MethodEditorBuilder mb = builder.editMethod();
        mb.targetMethod("channelRead0", "io.netty.channel.ChannelHandlerContext", "io.netty.handler.codec.http.FullHttpRequest");
        mb.injectInterceptor().interceptorClass("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ChannelRead0Interceptor");

        return builder.build();
    }
    
    
    private ClassEditor getNpcHandlerModifier(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("com.nhncorp.lucy.bloc.npc.handler.NpcHandler");
        
        MethodEditorBuilder mb = builder.editMethod();
        mb.targetMethod("messageReceived", "external.org.apache.mina.common.IoFilter$NextFilter", "external.org.apache.mina.common.IoSession", "java.lang.Object");
        mb.injectInterceptor().interceptorClass("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.MessageReceivedInterceptor");

        return builder.build();
    }

    private ClassEditor getRequestProcessorModifier(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.target("com.nhncorp.lucy.bloc.core.processor.RequestProcessor");
        
        MethodEditorBuilder mb = builder.editMethod();
        mb.targetMethod("process", "com.nhncorp.lucy.bloc.core.processor.BlocRequest");
        mb.injectInterceptor().interceptorClass("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ProcessInterceptor");

        return builder.build();
    }
    
    @Override
    public boolean isInstance() {
        if (isBloc4()) { 
            return true;
        }
        
        if (isBloc3()) {
            return true;
        }
        
        return false;
    }

    private boolean isBloc3() {
        if (bloc3Home != null) {
            return true;
        }
            
        String catalinaHome = System.getProperty("catalina.home");
        
        if (catalinaHome != null) {
            File bloc3CatalinaJar = new File(catalinaHome + "/server/lib/catalina.jar");
            File bloc3ServletApiJar = new File(catalinaHome + "/common/lib/servlet-api.jar");
            
            if (bloc3CatalinaJar.exists() && bloc3ServletApiJar.exists()) {
                bloc3Home = catalinaHome;
                return true;
            }
        }

        return false;
    }

    private boolean isBloc4() {
        if (bloc4Home != null) {
            return true;
        }
        
        String blocHome = System.getProperty("bloc.home");
                
        if (blocHome != null) {
            File home = new File(blocHome);
            
            if (home.exists() && home.isDirectory()) {
                bloc4Home = blocHome;
                return true;
            }
        }
        
        return false;
    }

    @Override
    public ServiceType getServerType() {
        return BlocServiceTypes.BLOC;
    }

    @Override
    public String[] getClassPath() {
        if (isBloc4()) {
            return new String[] { bloc4Home + "/libs" };
        }
        
        if (isBloc3()) {
            return new String[] { bloc3Home + "/server/lib/catalina.jar", bloc3Home + "/common/lib/servlet-api.jar" };
        }

        return new String[0];
    }

    @Override
    public boolean isPinpointAgentLifeCycleController() {
        if (isBloc4()) {
            return false;
        }
        
        if (isBloc3()) {
            return true;
        }
        
        return false;
    }
}