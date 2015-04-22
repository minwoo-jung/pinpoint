package com.navercorp.pinpoint.plugin.lucy.net;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassConditions;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerSetup;

/**
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Jongho Moon
 *
 */
public class LucyNetPlugin implements ProfilerPlugin, LucyNetConstants {

    @Override
    public void setup(ProfilerPluginContext context) {
        // lucy-net
        addCompositeInvocationFutureTransformer(context);
        addDefaultInvocationFutureTransformer(context);
        
        // nimm
        addNimmInvokerTransformer(context);
        
        // npc
        addKeepAliveNpcHessianConnectorTransformer(context);
        addLightWeightConnectorTransformer(context);
//        addLightWeightNbfpConnectorTransformer(context);
//        addLightWeightNpcHessianConnectorTransformer(context);
        addNioNpcHessianConnectorTransformer(context);
        addNpcHessianConnectorTransformer(context);
    }

    private void addCompositeInvocationFutureTransformer(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.net.invoker.CompositeInvocationFuture");

        // FIXME 이렇게 하면 api type이 internal method로 보이는데 사실 NPC_CLIENT, NIMM_CLIENT로 보여야함. servicetype으로 넣기에 애매해서. 어떻게 수정할 것인지는 나중에 고민.
        builder.editMethod("getReturnValue").injectInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor");
        context.addClassFileTransformer(builder.build());
    }
    
    private void addDefaultInvocationFutureTransformer(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.net.invoker.DefaultInvocationFuture");
        
        // FIXME 이렇게 하면 api type이 internal method로 보이는데 사실 NPC_CLIENT, NIMM_CLIENT로 보여야함. servicetype으로 넣기에 애매해서. 어떻게 수정할 것인지는 나중에 고민.
        builder.editMethod("getReturnValue").injectInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor");
        builder.editMethod("get").injectInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor");
        context.addClassFileTransformer(builder.build());
    }
    
    private void addNimmInvokerTransformer(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.nimm.connector.bloc.NimmInvoker");
        
        builder.injectMetadata(METADATA_NIMM_ADDRESS);

        // TODO nimm socket도 수집해야하나??
        // builder.injectMetadata("nimmSocket");
        
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor.NimmInvokerConstructorInterceptor");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor.InvokeMethodInterceptor");
        
        context.addClassFileTransformer(builder.build());
    }
    
    private void addKeepAliveNpcHessianConnectorTransformer(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.npc.connector.KeepAliveNpcHessianConnector");
        buildCommonConnectorTransformer(builder);

        builder.editConstructor("java.net.InetSocketAddress", "long", "long", "java.nio.charset.Charset").injectInterceptor("com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.ConnectorConstructorInterceptor");
        builder.editMethod("initializeConnector").injectInterceptor("com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.InitializeConnectorInterceptor");
        builder.editMethod("invokeImpl", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]").injectInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor");

        context.addClassFileTransformer(builder.build());
    }
    
    private void buildCommonConnectorTransformer(ClassFileTransformerBuilder builder) {
        builder.injectMetadata(METADATA_NPC_SERVER_ADDRESS);
        
        builder.editConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption").injectInterceptor("com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.ConnectorConstructorInterceptor");
        builder.editMethod("invoke", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]").injectInterceptor("com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.InvokeInterceptor");
    }
    
    private void addLightWeightConnectorTransformer(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.npc.connector.LightWeightConnector");
        buildCommonConnectorTransformer(builder);
        context.addClassFileTransformer(builder.build());
    }

    
    private void addLightWeightNbfpConnectorTransformer(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.npc.connector.LightWeightNbfpConnector");
        buildCommonConnectorTransformer(builder);
        context.addClassFileTransformer(builder.build());
    }

    private void addLightWeightNpcHessianConnectorTransformer(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.npc.connector.LightWeightNpcHessianConnector");
        buildCommonConnectorTransformer(builder);
        context.addClassFileTransformer(builder.build());
    }
    
    private void addNioNpcHessianConnectorTransformer(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.npc.connector.NioNpcHessianConnector");
        buildCommonConnectorTransformer(builder);
        context.addClassFileTransformer(builder.build());
    }
    
    private void addNpcHessianConnectorTransformer(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.npc.connector.NpcHessianConnector");

        builder.conditional(ClassConditions.hasDeclaredMethod("createConnecor", "com.nhncorp.lucy.npc.connector.NpcConnectorOption"), new ConditionalClassFileTransformerSetup() {
            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                conditional.editMethod("createConnecor", "com.nhncorp.lucy.npc.connector.NpcConnectorOption").injectInterceptor("com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.CreateConnectorInterceptor");
            }
        });
        
        builder.editMethod("invoke", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]").injectInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor");
        
        context.addClassFileTransformer(builder.build());
    }
}
