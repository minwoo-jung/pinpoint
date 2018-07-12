/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.namespace.NameSpaceInfo;
import com.navercorp.pinpoint.collector.namespace.RequestAttributes;
import com.navercorp.pinpoint.collector.namespace.RequestContextHolder;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author minwoo.jung
 */
public class NameSpaceDispatchHandlerWrapper implements DispatchHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DispatchHandler delegate;
    private final boolean useDefaultNameSapceInfo;


    public NameSpaceDispatchHandlerWrapper(boolean useDefaultNameSapceInfo, DispatchHandler delegate) {
        this.useDefaultNameSapceInfo = useDefaultNameSapceInfo;
        this.delegate = delegate;
    }

    @Override
    public void dispatchSendMessage(ServerRequest serverRequest) {
        try {
            beforeInterceptor(serverRequest);
            delegate.dispatchSendMessage(serverRequest);
        } finally {
            afterInterceptor();
        }

    }

    @Override
    public void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse serverResponse) {
        try {
            beforeInterceptor(serverRequest);
            delegate.dispatchRequestMessage(serverRequest, serverResponse);
        } finally {
            afterInterceptor();
        }
    }


    private void beforeInterceptor(ServerRequest serverRequest) {
        NameSpaceInfo nameSpaceInfo = (NameSpaceInfo) serverRequest.getAttribute(NameSpaceInfo.NAMESPACE_INFO);

        if (nameSpaceInfo == null) {
            if (useDefaultNameSapceInfo) {
                nameSpaceInfo = new NameSpaceInfo("navercorp", "pinpoint", "default");
            } else {
                throw new IllegalStateException("can not find NamespaceInfo" + serverRequest);
            }
        }

        RequestAttributes requestAttributes = new RequestAttributes(new ConcurrentHashMap<>());
        requestAttributes.setAttribute(NameSpaceInfo.NAMESPACE_INFO, nameSpaceInfo);
        RequestContextHolder.setAttributes(requestAttributes);

        if (logger.isDebugEnabled()) {
            logger.debug("initialed RequestContextHolder for NamespaceInfo : {}", nameSpaceInfo);
        }
    }

    private void afterInterceptor() {
        if (logger.isDebugEnabled()) {
            RequestAttributes attributes = RequestContextHolder.currentAttributes();
            NameSpaceInfo nameSpaceInfo = (NameSpaceInfo) attributes.getAttribute(NameSpaceInfo.NAMESPACE_INFO);
            logger.debug("reset RequestContextHolder for NamespaceInfo : {}", nameSpaceInfo);
        }

        RequestContextHolder.resetAttributes();
    }
}
