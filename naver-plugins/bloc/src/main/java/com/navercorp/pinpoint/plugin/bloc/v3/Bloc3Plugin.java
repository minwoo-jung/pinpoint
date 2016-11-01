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
import com.navercorp.pinpoint.plugin.bloc.BlocConstants;
import com.navercorp.pinpoint.plugin.bloc.BlocPluginConfig;

import java.security.ProtectionDomain;

public class Bloc3Plugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final BlocPluginConfig config = new BlocPluginConfig(context.getConfig());

        if (!config.isBlocEnable()) {
            logger.info("BlocPlugin disabled");
            return;
        }
        Bloc3Detector bloc3Detector = new Bloc3Detector(config.getBlocBootstrapMains());
        context.addApplicationTypeDetector(bloc3Detector);

        addBlocAdapterEditor();
        addNimmHandlerModifider();
    }

    private void addBlocAdapterEditor() {
        transformTemplate.transform("com.nhncorp.lucy.bloc.handler.HTTPHandler$BlocAdapter", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addInterceptor("com.navercorp.pinpoint.plugin.bloc.v3.interceptor.ExecuteMethodInterceptor");
                return target.toBytecode();
            }
        });
    }

    private void addNimmHandlerModifider() {
        transformTemplate.transform("com.nhncorp.lucy.bloc.handler.NimmHandler$ContextWorker", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(BlocConstants.NIMM_SERVER_SOCKET_ADDRESS_ACCESSOR);

                InstrumentMethod setPrefableNetEndPoint = target.addDelegatorMethod("setPrefableNetEndPoint", "com.nhncorp.lucy.nimm.connector.NimmNetEndPoint");
                if (setPrefableNetEndPoint != null) {
                    setPrefableNetEndPoint.addInterceptor("com.navercorp.pinpoint.plugin.bloc.v4.interceptor.NimmAbstractWorkerInterceptor");
                }

                target.addInterceptor("com.navercorp.pinpoint.plugin.bloc.v3.interceptor.NimmHandlerInterceptor");
                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
