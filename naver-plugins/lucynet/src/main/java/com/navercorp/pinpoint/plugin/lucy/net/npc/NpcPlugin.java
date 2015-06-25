package com.navercorp.pinpoint.plugin.lucy.net.npc;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConstructorTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerBuilder;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;

/**
 * @author Taejin Koo
 */
public abstract class NpcPlugin {

    final ProfilerPluginContext context;

    public NpcPlugin(ProfilerPluginContext context) {
        this.context = context;
    }

    abstract String getEditClazzName();
    abstract void addRecipe();
    
    void editConstructor(ConditionalClassFileTransformerBuilder conditional, String[] parameterTypeNames, String interceptorClassName) {
        ConstructorTransformerBuilder constructorBuilder = conditional.editConstructor(parameterTypeNames);
        constructorBuilder.injectInterceptor(interceptorClassName);
    }

    void editMethod(ConditionalClassFileTransformerBuilder conditional, String methodName, String[] parameterTypeNames, String interceptorClassName, Object... constructorArguments) {
          MethodTransformerBuilder methodBuilder = conditional.editMethod(methodName, parameterTypeNames);
          methodBuilder.injectInterceptor(interceptorClassName, constructorArguments);
    }
    
    void editInternalInvokeMethod(ConditionalClassFileTransformerBuilder conditional) {
        MethodTransformerBuilder methodBuilder = conditional.editMethod("invoke", new String[] { "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]" });
        methodBuilder.injectInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor", LucyNetConstants.NPC_CLIENT_INTERNAL);

        methodBuilder = conditional.editMethod("invoke", new String[] { "java.lang.String", "java.lang.String", "java.lang.Object[]" });
        methodBuilder.injectInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor", LucyNetConstants.NPC_CLIENT_INTERNAL);
    }
    
    void editConnectorConstructor(ConditionalClassFileTransformerBuilder conditional) {
        ConstructorTransformerBuilder constructorBuilder = conditional.editConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption");
        constructorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.ConnectorConstructorInterceptor");
    }

    void editInitializeConnectorMethod(ConditionalClassFileTransformerBuilder conditional) {
        MethodTransformerBuilder methodBuilder = conditional.editMethod("initializeConnector");
        methodBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.InitializeConnectorInterceptor");
    }
}
