package com.navercorp.pinpoint.plugin.bloc.v4;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;
import com.navercorp.pinpoint.plugin.bloc.BlocPluginConfig;
import com.navercorp.pinpoint.plugin.bloc.InterceptorConstants;

import java.security.ProtectionDomain;

public class Bloc4Plugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final BlocPluginConfig config = new BlocPluginConfig(context.getConfig());

        if (!config.isBlocEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            final Bloc4Detector bloc4Detector = new Bloc4Detector(config.getBlocBootstrapMains());
            if (bloc4Detector.detect()) {
                logger.info("Detected application type : {}", BlocConstants.BLOC);
                if (!context.registerApplicationType(BlocConstants.BLOC)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), BlocConstants.BLOC);
                }
            }
        }

        logger.info("Adding Bloc4 transformers");
        addNettyInboundHandlerModifier();
        addNpcHandlerModifier();
        addNimmHandlerModifider();
        addRequestProcessorModifier();
        addModuleClassLoaderFactoryInterceptor();
    }

    private void addNettyInboundHandlerModifier() {
        transformTemplate.transform("com.nhncorp.lucy.bloc.http.NettyInboundHandler", NettyInboundHandlerTransform.class);
    }

    public static class NettyInboundHandlerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addGetter(com.navercorp.pinpoint.plugin.bloc.v4.UriEncodingGetter.class, "uriEncoding");

            final InstrumentMethod channelRead0Method = InstrumentUtils.findMethod(target, "channelRead0", "io.netty.channel.ChannelHandlerContext", "io.netty.handler.codec.http.FullHttpRequest");
            channelRead0Method.addInterceptor(com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ChannelRead0Interceptor.class);

            return target.toBytecode();
        }
    }

    private void addNpcHandlerModifier() {
        transformTemplate.transform("com.nhncorp.lucy.bloc.npc.handler.NpcHandler", NpcHandlerTransform.class);
    }

    public static class NpcHandlerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod messageReceivedMethod = InstrumentUtils.findMethod(target, "messageReceived", "external.org.apache.mina.common.IoFilter$NextFilter", "external.org.apache.mina.common.IoSession", "java.lang.Object");
            messageReceivedMethod.addInterceptor(com.navercorp.pinpoint.plugin.bloc.v4.interceptor.MessageReceivedInterceptor.class);

            return target.toBytecode();
        }
    }

    private void addNimmHandlerModifider() {
        transformTemplate.transform("com.nhncorp.lucy.bloc.nimm.handler.NimmHandler$ContextWorker", NimmHandlerContextWorkerTransform.class);
    }

    public static class NimmHandlerContextWorkerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(InterceptorConstants.NIMM_SERVER_SOCKET_ADDRESS_ACCESSOR);

            InstrumentMethod setPrefableNetEndPoint = target.addDelegatorMethod("setPrefableNetEndPoint", "com.nhncorp.lucy.nimm.connector.NimmNetEndPoint");
            if (setPrefableNetEndPoint != null) {
                setPrefableNetEndPoint.addInterceptor(com.navercorp.pinpoint.plugin.bloc.v4.interceptor.NimmAbstractWorkerInterceptor.class);
            }

            final InstrumentMethod handleResponseMessageMethod = target.getDeclaredMethod("handleResponseMessage", "com.nhncorp.lucy.npc.NpcMessage", "java.lang.String");
            if (handleResponseMessageMethod != null) {
                handleResponseMessageMethod.addInterceptor(com.navercorp.pinpoint.plugin.bloc.v4.interceptor.NimmHandlerInterceptor.class);
            } else {
                PLogger logger = PLoggerFactory.getLogger(this.getClass());
                logger.info("Bloc 4.x does not support profiling for NIMM requests in versions of NIMM 2.2.11 and earlier.");
            }

            return target.toBytecode();
        }
    }

    private void addRequestProcessorModifier() {
        transformTemplate.transform("com.nhncorp.lucy.bloc.core.processor.RequestProcessor", RequestProcessorTransform.class);
    }

    public static class RequestProcessorTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod processMethod = InstrumentUtils.findMethod(target, "process", "com.nhncorp.lucy.bloc.core.processor.BlocRequest");
            processMethod.addInterceptor(com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ProcessInterceptor.class);

            return target.toBytecode();
        }
    }

    private void addModuleClassLoaderFactoryInterceptor() {
        transformTemplate.transform("com.nhncorp.lucy.bloc.core.clazz.ModuleClassLoaderFactory", ModuleClassLoaderFactoryTransform.class);
    }

    public static class ModuleClassLoaderFactoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod constructorMethod = InstrumentUtils.findConstructor(target);
            constructorMethod.addInterceptor(com.navercorp.pinpoint.plugin.bloc.v4.interceptor.ModuleClassLoaderFactoryInterceptor.class);

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
