/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.grpc.security.server;

import com.navercorp.pinpoint.grpc.security.SecurityConstants;

import io.grpc.Attributes;
import io.grpc.ServerTransportFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class SecurityServerTransportFilter extends ServerTransportFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Attributes transportReady(final Attributes attributes) {
        if (logger.isDebugEnabled()) {
            logger.debug("Ready attributes={}", attributes);
        }

        Attributes.Builder attributesBuilder = attributes.toBuilder();
        attributesBuilder.set(SecurityConstants.SECURITY_CONTEXT, new DefaultSecurityContext());
        return attributesBuilder.build();
    }

    @Override
    public void transportTerminated(Attributes transportAttrs) {
        if (logger.isDebugEnabled()) {
            logger.debug("Terminated attributes={}", transportAttrs);
        }
    }

}
