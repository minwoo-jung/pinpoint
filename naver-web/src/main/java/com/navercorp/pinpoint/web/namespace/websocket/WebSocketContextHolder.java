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
package com.navercorp.pinpoint.web.namespace.websocket;

import org.springframework.core.NamedThreadLocal;

/**
 * @author minwoo.jung
 */
public class WebSocketContextHolder {

    private static final ThreadLocal<WebSocketAttributes> webSocketAttributesHolder = new NamedThreadLocal<>("WebSocket attributes");

    public static void resetAttributes() {
        WebSocketAttributes attributes = webSocketAttributesHolder.get();

        if (attributes != null) {
            attributes.clear();
        }

        webSocketAttributesHolder.remove();
    }

    public static WebSocketAttributes getAttributes() {
        return webSocketAttributesHolder.get();
    }

    public static void setAttributes(WebSocketAttributes attributes) {
        if (attributes == null) {
            resetAttributes();
        } else {
            webSocketAttributesHolder.set(attributes);
        }
    }

    public static WebSocketAttributes currentAttributes() throws IllegalStateException {
        WebSocketAttributes attributes = webSocketAttributesHolder.get();

        if (attributes == null) {
            throw new IllegalStateException("No thread-bound websocketfound ");
        }

        return attributes;
    }

}
