/*
 * Copyright 2014 NAVER Corp.
 *
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
package com.navercorp.pinpoint.plugin.nbasearc.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;

/**
 * Gateway(nBase-ARC client) getServer() method interceptor 
 * - trace destinationId
 * 
 * @author jaehong.kim
 *
 */
public class GatewayGetServerMethodInterceptor extends GatewayServerMetadataAttachInterceptor {

    public GatewayGetServerMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, @Name(METADATA_DESTINATION_ID) MetadataAccessor destinationIdAccessor) {
        super(traceContext, methodDescriptor, destinationIdAccessor);
    }
}