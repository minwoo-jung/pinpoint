/*
 * Copyright 2014 NAVER Corp.
 *
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
package com.navercorp.pinpoint.plugin.nelo;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassConditions;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerSetup;

/**
 * nelo 서버에 로그 전달 여부를 수집한다.
 * 
 * nelo에서 관리하는 log message를 담고있는 buffer가 차서 log를 전달 못하거나 nelo 서버와 통신 시 에러가 발생하는 경우 
 * 이런 경우를 pinpoint가 정확히 감지하는것은 불필요 연산과 클래스 변경이 너무 많이 개입되므로 이런 경우는 고려하지 않는다.
 * 
 * NeloAsyncAppender를 사용하면서 NeloAppender에 log level filter를 사용하는 경우는 고려하지 않는다. 
 * NeloAppender는 별도의 thread에 동작하므로 pinpoint에서 정보를 획득할수 없다.  
 * 
 * @author minwoo.jung
 */
public class NeloPlugin implements ProfilerPlugin {

    @Override
    public void setup(ProfilerPluginContext context) {
        final NeloPluginConfig config = new NeloPluginConfig(context.getConfig());
        
        if (config.isLog4jLoggingTransactionInfo()) {
            addLog4jNelo2AsyncAppenderEditor(context);
            addLog4jNeloAppenderEditor(context);
        }
        
        if (config.isLogBackLoggingTransactionInfo()) {
            addLogBackNelo2AsyncAppenderEditor(context);
            addLogBackNeloAppenderEditor(context);
        }
    }
    
    private void addLogBackNeloAppenderEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("com.nhncorp.nelo2.logback.NeloLogbackAppender");
        classEditorBuilder.editMethod("append", new String[]{"ch.qos.logback.classic.spi.ILoggingEvent"}).injectInterceptor("com.navercorp.pinpoint.plugin.nelo.interceptor.AppenderInterceptor");
        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void addLogBackNelo2AsyncAppenderEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("com.nhncorp.nelo2.logback.LogbackAsyncAppender");
        classEditorBuilder.editMethod("append", new String[]{"java.lang.Object"}).injectInterceptor("com.navercorp.pinpoint.plugin.nelo.interceptor.AppenderInterceptor");
        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void addLog4jNelo2AsyncAppenderEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("com.nhncorp.nelo2.log4j.Nelo2AsyncAppender");
        classEditorBuilder.conditional(ClassConditions.hasNotDeclaredMethod("append", new String[]{"org.apache.log4j.spi.LoggingEvent"}), new ConditionalClassFileTransformerSetup() {
           
            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                conditional.overrideMethodToDelegate("append", new String[]{"org.apache.log4j.spi.LoggingEvent"});
            }
            
        });
        
        classEditorBuilder.editMethod("append", new String[]{"org.apache.log4j.spi.LoggingEvent"}).injectInterceptor("com.navercorp.pinpoint.plugin.nelo.interceptor.AppenderInterceptor");
        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void addLog4jNeloAppenderEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("com.nhncorp.nelo2.log4j.NeloAppender");
        classEditorBuilder.editMethod("append", new String[]{"org.apache.log4j.spi.LoggingEvent"}).injectInterceptor("com.navercorp.pinpoint.plugin.nelo.interceptor.AppenderInterceptor");
        context.addClassFileTransformer(classEditorBuilder.build());
    }
}
