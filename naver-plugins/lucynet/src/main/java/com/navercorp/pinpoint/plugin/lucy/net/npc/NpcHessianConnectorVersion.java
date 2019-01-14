package com.navercorp.pinpoint.plugin.lucy.net.npc;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.plugin.lucy.net.InterceptorConstants;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetPlugin;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetUtils;

/**
 * @author Taejin Koo
 */
public enum NpcHessianConnectorVersion {

    V13to {
        @Override
        public boolean checkCondition(InstrumentClass target) {
            return target.hasDeclaredMethod("createConnecor", "com.nhncorp.lucy.npc.connector.NpcConnectorOption");
        }

        @Override
        public byte[] transform(InstrumentClass target) throws InstrumentException {
            InstrumentMethod method = target.getDeclaredMethod("createConnecor", "com.nhncorp.lucy.npc.connector.NpcConnectorOption");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.NPC_CONSTRUCTOR_INTERCEPTOR);

            method = target.getDeclaredMethod("invoke", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.BASIC_INTERCEPTOR, LucyNetConstants.NPC_CLIENT_INTERNAL);

            method = target.getDeclaredMethod("invoke", "java.lang.String", "java.lang.String", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.BASIC_INTERCEPTOR, LucyNetConstants.NPC_CLIENT_INTERNAL);

            return target.toBytecode();
        }
    },

    V12 {
        @Override
        public boolean checkCondition(InstrumentClass target) {
            return target.hasDeclaredMethod("initialize", "com.nhncorp.lucy.npc.connector.NpcConnectorOption");
        }

        @Override
        public byte[] transform(InstrumentClass target) throws InstrumentException {
            InstrumentMethod method = target.getDeclaredMethod("initialize", "com.nhncorp.lucy.npc.connector.NpcConnectorOption");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.NPC_CREATE_CONNECTOR_INTERCEPTOR);

            method = target.getDeclaredMethod("invoke", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.BASIC_INTERCEPTOR, LucyNetConstants.NPC_CLIENT_INTERNAL);

            method = target.getDeclaredMethod("invoke", "java.lang.String", "java.lang.String", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.BASIC_INTERCEPTOR, LucyNetConstants.NPC_CLIENT_INTERNAL);

            return target.toBytecode();
        }
    },

    V11 {

        @Override
        public boolean checkCondition(InstrumentClass target) {
            return target.hasConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption");
        }

        @Override
        public byte[] transform(InstrumentClass target) throws InstrumentException {
            InstrumentMethod constructor = target.getConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption");
            LucyNetUtils.addInterceptor(constructor, InterceptorConstants.NPC_CONSTRUCTOR_INTERCEPTOR);

            InstrumentMethod method = target.getDeclaredMethod("invoke", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.BASIC_INTERCEPTOR, LucyNetConstants.NPC_CLIENT_INTERNAL);

            method = target.getDeclaredMethod("invoke", "java.lang.String", "java.lang.String", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.BASIC_INTERCEPTOR, LucyNetConstants.NPC_CLIENT_INTERNAL);

            return target.toBytecode();
        }
    },

    V6to10 {
        @Override
        public boolean checkCondition(InstrumentClass target) {
            if (!target.hasConstructor("java.net.InetSocketAddress", "boolean", "boolean", "boolean", "java.nio.charset.Charset", "long")) {
                return false;
            }
            if (!target.hasConstructor("java.net.InetSocketAddress", "com.nhncorp.lucy.npc.connector.ConnectionFactory")) {
                return false;
            }

            return true;
        }

        @Override
        public byte[] transform(InstrumentClass target) throws InstrumentException {
            InstrumentMethod constructor = target.getConstructor("java.net.InetSocketAddress", "boolean", "boolean", "boolean", "java.nio.charset.Charset", "long");
            LucyNetUtils.addInterceptor(constructor, InterceptorConstants.NPC_OLD_CONSTRUCTOR_INTERCEPTOR);

            constructor = target.getConstructor("java.net.InetSocketAddress", "com.nhncorp.lucy.npc.connector.ConnectionFactory");
            LucyNetUtils.addInterceptor(constructor, InterceptorConstants.NPC_OLD_CONSTRUCTOR_INTERCEPTOR);

            InstrumentMethod method = target.getDeclaredMethod("invoke", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.NPC_INVOKE_INTERCEPTOR);

            return target.toBytecode();
        }
    },

    V5 {
        @Override
        public boolean checkCondition(InstrumentClass target) {
            return target.hasConstructor("java.net.InetSocketAddress", "boolean", "boolean", "boolean", "java.nio.charset.Charset", "long");
        }

        @Override
        public byte[] transform(InstrumentClass target) throws InstrumentException {
            InstrumentMethod constructor = target.getConstructor("java.net.InetSocketAddress", "boolean", "boolean", "boolean", "java.nio.charset.Charset", "long");
            LucyNetUtils.addInterceptor(constructor, InterceptorConstants.NPC_OLD_CONSTRUCTOR_INTERCEPTOR);

            InstrumentMethod method = target.getDeclaredMethod("invoke", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.NPC_INVOKE_INTERCEPTOR);

            return target.toBytecode();
        }
    };


    public abstract boolean checkCondition(InstrumentClass target);

    public abstract byte[] transform(InstrumentClass target) throws InstrumentException;

}
