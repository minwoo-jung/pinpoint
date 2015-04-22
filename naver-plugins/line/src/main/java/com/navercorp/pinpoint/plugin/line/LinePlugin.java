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

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;

/**
 * @author Jongho Moon
 *
 */
public class LinePlugin implements ProfilerPlugin, LineConstants {

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin#setup(com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext)
     */
    @Override
    public void setup(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("com.linecorp.games.common.baseFramework.handlers.HttpCustomServerHandler$InvokeTask");

        builder.injectMetadata(CHANNEL_HANDLER_CONTEXT);
        builder.injectMetadata(MESSAGE_EVENT);
        
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.line.games.interceptor.InvokeTaskConstructorInterceptor");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.line.games.interceptor.InvokeTaskRunInterceptor");
        
        context.addClassFileTransformer(builder.build());
    }
}
