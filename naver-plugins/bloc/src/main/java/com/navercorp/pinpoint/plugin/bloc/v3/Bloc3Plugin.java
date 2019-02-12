package com.navercorp.pinpoint.plugin.bloc.v3;

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

public class Bloc3Plugin implements ProfilerPlugin, TransformTemplateAware {

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
            final Bloc3Detector bloc3Detector = new Bloc3Detector(config.getBlocBootstrapMains());
            if (bloc3Detector.detect()) {
                logger.info("Detected application type : {}", BlocConstants.BLOC);
                if (!context.registerApplicationType(BlocConstants.BLOC)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), BlocConstants.BLOC);
                }
            }
        }

        logger.info("Adding Bloc3 transformers");
        addBlocAdapterEditor();
        addNpcHandlerModifier();
        addNimmHandlerModifider();
    }

    private void addBlocAdapterEditor() {
        transformTemplate.transform("com.nhncorp.lucy.bloc.handler.HTTPHandler$BlocAdapter", HTTPHandlerBlocAdapterTransform.class);
    }

    public static class HTTPHandlerBlocAdapterTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod executeMethod = InstrumentUtils.findMethod(target, "execute", "external.org.apache.coyote.Request", "external.org.apache.coyote.Response");
            executeMethod.addInterceptor(com.navercorp.pinpoint.plugin.bloc.v3.interceptor.ExecuteMethodInterceptor.class);

            return target.toBytecode();
        }
    }

    private void addNpcHandlerModifier() {
        transformTemplate.transform("com.nhncorp.lucy.bloc.handler.NPCHandler", NPCHandlerTransform.class);
    }

    public static class NPCHandlerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // BLOC3, BLOC4 모두 MINA의 IoFilterAdapter를 상속하고 있기 때문에 v4의 MessageReceivedInterceptor를 그대로 사용 합니다

            final InstrumentMethod messageReceivedMethod = InstrumentUtils.findMethod(target, "messageReceived", "external.org.apache.mina.common.IoFilter$NextFilter", "external.org.apache.mina.common.IoSession", "java.lang.Object");
            messageReceivedMethod.addInterceptor(com.navercorp.pinpoint.plugin.bloc.v4.interceptor.MessageReceivedInterceptor.class);

            return target.toBytecode();
        }
    }

    private void addNimmHandlerModifider() {
        transformTemplate.transform("com.nhncorp.lucy.bloc.handler.NimmHandler$ContextWorker", NimmHandlerContextWorkerTransform.class);
    }

    public static class NimmHandlerContextWorkerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(InterceptorConstants.NIMM_SERVER_SOCKET_ADDRESS_ACCESSOR);

            final InstrumentMethod setPrefableNetEndPoint = target.addDelegatorMethod("setPrefableNetEndPoint", "com.nhncorp.lucy.nimm.connector.NimmNetEndPoint");
            if (setPrefableNetEndPoint != null) {
                setPrefableNetEndPoint.addInterceptor(com.navercorp.pinpoint.plugin.bloc.v4.interceptor.NimmAbstractWorkerInterceptor.class);
            }

            final InstrumentMethod handleResponseMessageMethod = target.getDeclaredMethod("handleResponseMessage", "com.nhncorp.lucy.npc.NpcMessage", "com.nhncorp.lucy.nimm.connector.address.NimmAddress");
            if (handleResponseMessageMethod != null) {
                handleResponseMessageMethod.addInterceptor(com.navercorp.pinpoint.plugin.bloc.v3.interceptor.NimmHandlerInterceptor.class);
            } else {
                PLogger logger = PLoggerFactory.getLogger(this.getClass());
                logger.info("Bloc 3.x does not support profiling for NIMM requests in versions of NIMM 2.2.11 and earlier.");
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
