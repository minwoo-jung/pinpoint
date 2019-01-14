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
public enum KeepAliveVersion {

    V13to {

        @Override
        public boolean checkCondition(InstrumentClass target) {
            if (!target.hasConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption")) {
                return false;
            }
            if (!target.hasDeclaredMethod("invokeImpl", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]")) {
                return false;
            }

            return true;
        }

        @Override
        public byte[] transform(InstrumentClass target) throws InstrumentException {
            InstrumentMethod constructor = target.getConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption");
            LucyNetUtils.addInterceptor(constructor, InterceptorConstants.NPC_CONSTRUCTOR_INTERCEPTOR);

            constructor = target.getConstructor("java.net.InetSocketAddress", "long", "long", "java.nio.charset.Charset");
            LucyNetUtils.addInterceptor(constructor, InterceptorConstants.NPC_CONSTRUCTOR_INTERCEPTOR);

            InstrumentMethod method = target.getDeclaredMethod("initializeConnector");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.NPC_INIT_CONNECTOR_INTERCEPTOR);

            method = target.getDeclaredMethod("invokeImpl", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.BASIC_INTERCEPTOR, LucyNetConstants.NPC_CLIENT_INTERNAL);

            return target.toBytecode();
        }

    },

    V11toV12 {

        @Override
        public boolean checkCondition(InstrumentClass target) {
            if (!target.hasConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption")) {
                return false;
            }
            if (!target.hasDeclaredMethod("invokeImpl", "java.lang.String", "java.lang.String", "java.lang.Object[]")) {
                return false;
            }

            return true;
        }

        @Override
        public byte[] transform(InstrumentClass target) throws InstrumentException {
            InstrumentMethod constructor = target.getConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption");
            LucyNetUtils.addInterceptor(constructor, InterceptorConstants.NPC_CONSTRUCTOR_INTERCEPTOR);

            InstrumentMethod method = target.getDeclaredMethod("initializeConnector");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.NPC_INIT_CONNECTOR_INTERCEPTOR);

            method = target.getDeclaredMethod("invokeImpl", "java.lang.String", "java.lang.String", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.BASIC_INTERCEPTOR, LucyNetConstants.NPC_CLIENT_INTERNAL);

            return target.toBytecode();
        }

    },

    V8toV10 {

        @Override
        public boolean checkCondition(InstrumentClass target) {
            if (!target.hasConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption")) {
                return false;
            }

            return true;
        }

        @Override
        public byte[] transform(InstrumentClass target) throws InstrumentException {
            InstrumentMethod constructor = target.getConstructor("java.net.InetSocketAddress", "long", "long", "java.nio.charset.Charset");
            LucyNetUtils.addInterceptor(constructor, InterceptorConstants.NPC_CONSTRUCTOR_INTERCEPTOR);

            InstrumentMethod method = target.getDeclaredMethod("initializeConnector");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.NPC_INIT_CONNECTOR_INTERCEPTOR);

            method = target.getDeclaredMethod("invoke", "java.lang.String", "java.lang.String", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.BASIC_INTERCEPTOR, LucyNetConstants.NPC_CLIENT_INTERNAL);

            return target.toBytecode();
        }

    };

    public abstract boolean checkCondition(InstrumentClass target);

    public abstract byte[] transform(InstrumentClass target) throws InstrumentException;

}
