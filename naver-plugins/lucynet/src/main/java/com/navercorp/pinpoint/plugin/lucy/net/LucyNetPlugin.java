package com.navercorp.pinpoint.plugin.lucy.net;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerProperty;
import com.navercorp.pinpoint.plugin.lucy.net.npc.NpcPluginHolder;

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
        
        NpcPluginHolder npcPlugin = new NpcPluginHolder(context);
        npcPlugin.addPlugin();
    }

    private void addCompositeInvocationFutureTransformer(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.net.invoker.CompositeInvocationFuture");

        // FIXME 이렇게 하면 api type이 internal method로 보이는데 사실 NPC_CLIENT, NIMM_CLIENT로 보여야함. servicetype으로 넣기에 애매해서. 어떻게 수정할 것인지는 나중에 고민.
        builder.editMethod("getReturnValue").injectInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor");
        context.addClassFileTransformer(builder.build());
    }
    
    private void addDefaultInvocationFutureTransformer(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.nhncorp.lucy.net.invoker.DefaultInvocationFuture");
        builder.injectMetadata(METADATA_ASYNC_TRACE_ID);
        
        // FIXME 이렇게 하면 api type이 internal method로 보이는데 사실 NPC_CLIENT, NIMM_CLIENT로 보여야함. servicetype으로 넣기에 애매해서. 어떻게 수정할 것인지는 나중에 고민.
        // builder.editMethod("getReturnValue").injectInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor");
        // builder.editMethod("get").injectInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor");

        MethodTransformerBuilder methodBuilder = builder.editMethods(MethodFilters.name("getReturnValue", "get", "isReadyAndSet"));
        methodBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.lucy.net.interceptor.DefaultInvocationFutureMethodInterceptor");

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

}
