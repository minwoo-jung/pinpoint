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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * @author minwoo.jung
 */
public class WebSocketScope implements Scope {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        WebSocketAttributes webSocketAttributes = WebSocketContextHolder.currentAttributes();
        Object value = webSocketAttributes.getAttribute(name);

        if (value != null) {
            return value;
        }

        value = objectFactory.getObject();
        webSocketAttributes.setAttribute(name, value);

        return value;
    }

    @Override
    public Object remove(String name) {
        WebSocketAttributes webSocketAttributes = WebSocketContextHolder.currentAttributes();
        Object value = webSocketAttributes.getAttribute(name);

        if (value != null) {
            webSocketAttributes.removeAttribute(name);
            return value;
        }
        else {
            return null;
        }
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        throw new IllegalStateException("WebSocketScope does not support destruction callbacks.");
    }

    @Override
    public Object resolveContextualObject(String key) {
        throw new IllegalStateException("WebSocketScope does not support resolveContextualObject.");
    }

    @Override
    public String getConversationId() {
        throw new IllegalStateException("WebSocketScope does not support getConversationId.");
    }

}

