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
package com.navercorp.pinpoint.plugin.line;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

/**
 * @author Jongho Moon
 *
 */
public class LinePlugin implements ProfilerPlugin {

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin#setup(com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext)
     */
    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final LineConfig config = new LineConfig(context.getConfig());
        
        context.addClassFileTransformer("com.linecorp.games.common.baseFramework.handlers.HttpCustomServerHandler$InvokeTask", new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                
                target.addField("com.navercorp.pinpoint.plugin.line.ChannelHandlerContextAccessor");
                target.addField("com.navercorp.pinpoint.plugin.line.MessageEventAccessor");

                target.addInterceptor("com.navercorp.pinpoint.plugin.line.games.interceptor.InvokeTaskConstructorInterceptor");
                target.addInterceptor("com.navercorp.pinpoint.plugin.line.games.interceptor.InvokeTaskRunInterceptor", config.getParamDumpSize(), config.getEntityDumpSize());

                return target.toBytecode();
            }
        });
    }
}
