package com.navercorp.pinpoint.plugin.nelo;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassConditions;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerSetup;

public class NeloPlugin implements ProfilerPlugin {

    @Override
    public void setup(ProfilerPluginContext context) {
        final NeloPluginConfig config = new NeloPluginConfig(context.getConfig());
        
        if (config.isLog4jLoggingTransactionInfo()) {
            addNelo2AsyncAppenderEditor(context);
            addNeloAppenderEditor(context);
        }
    }
    
    private void addNelo2AsyncAppenderEditor(ProfilerPluginContext context) {
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

    private void addNeloAppenderEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("com.nhncorp.nelo2.log4j.NeloAppender");
        classEditorBuilder.editMethod("append", new String[]{"org.apache.log4j.spi.LoggingEvent"}).injectInterceptor("com.navercorp.pinpoint.plugin.nelo.interceptor.AppenderInterceptor");
        context.addClassFileTransformer(classEditorBuilder.build());
    }
}
