/**
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.line;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import com.navercorp.pinpoint.plugin.line.games.interceptor.InvokeTaskConstructorInterceptor;
import com.navercorp.pinpoint.plugin.line.games.interceptor.InvokeTaskRunInterceptor;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Jongho Moon
 *
 */
public class LinePlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin#setup(com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext)
     */
    @Override
    public void setup(ProfilerPluginSetupContext context) {
        transformTemplate.transform("com.linecorp.games.common.baseFramework.handlers.HttpCustomServerHandler$InvokeTask", InvokeTaskTransform.class);
    }

    public static class InvokeTaskTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(ChannelHandlerContextAccessor.class);
            target.addField(MessageEventAccessor.class);

            final InstrumentMethod constructorMethod = InstrumentUtils.findConstructor(target, "com.linecorp.games.common.baseFramework.handlers.HttpCustomServerHandler", "org.jboss.netty.channel.ChannelHandlerContext", "org.jboss.netty.channel.MessageEvent");
            constructorMethod.addInterceptor(InvokeTaskConstructorInterceptor.class);

            final InstrumentMethod runMethod = InstrumentUtils.findMethod(target, "run");
            final LineConfig config = new LineConfig(instrumentor.getProfilerConfig());
            runMethod.addInterceptor(InvokeTaskRunInterceptor.class, va(config.getParamDumpSize(), config.getEntityDumpSize()));

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
