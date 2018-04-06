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

package com.navercorp.pinpoint.web.websocket;

//import com.navercorp.pinpoint.web.namespace.websocket.WebSocketAttributes;
//import com.navercorp.pinpoint.web.namespace.websocket.WebSocketContextHolder;
//import com.navercorp.pinpoint.web.task.TimerTaskDecorator;
//import com.navercorp.pinpoint.web.task.TimerTaskDecoratorFactory;
import org.springframework.stereotype.Component;

//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//import java.util.TimerTask;

/**
 * @author HyunGil Jeong
 */
@Component
public class PaaSWebSocketTimerTaskDecoratorFactory {
//public class PaaSWebSocketTimerTaskDecoratorFactory implements TimerTaskDecoratorFactory {
//
//    private final PinpointWebSocketTimerTaskDecoratorFactory pinpointWebSocketTimerTaskDecoratorFactory = new PinpointWebSocketTimerTaskDecoratorFactory();
//
//    @Override
//    public TimerTaskDecorator createTimerTaskDecorator() {
//        TimerTaskDecorator decorator = pinpointWebSocketTimerTaskDecoratorFactory.createTimerTaskDecorator();
//        return new WebSocketContextPreservingTimerTaskDecorator(decorator);
//    }
//
//    private static class WebSocketContextPreservingTimerTaskDecorator implements TimerTaskDecorator {
//
//        private final TimerTaskDecorator pinpointWebSocketTimerTaskDecorator;
//        private final Map<String, Object> attributes;
//
//        private WebSocketContextPreservingTimerTaskDecorator(TimerTaskDecorator pinpointWebSocketTimerTaskDecorator) {
//            this.pinpointWebSocketTimerTaskDecorator = Objects.requireNonNull(pinpointWebSocketTimerTaskDecorator, "pinpointWebSocketTimerTaskDecorator must not be null");
//
//            WebSocketAttributes webSocketAttributes = WebSocketContextHolder.getAttributes();
//            if (webSocketAttributes == null) {
//                this.attributes = Collections.emptyMap();
//            } else {
//                this.attributes = new HashMap<>(webSocketAttributes.getAttributes());
//            }
//        }
//
//        @Override
//        public TimerTask decorate(TimerTask timerTask) {
//            return new TimerTask() {
//                @Override
//                public void run() {
//                    WebSocketAttributes previousWebSocketAttributes = WebSocketContextHolder.getAttributes();
//                    Map<String, Object> copy = new HashMap<>(attributes);
//                    WebSocketAttributes webSocketAttributes = new WebSocketAttributes(copy);
//                    WebSocketContextHolder.setAttributes(webSocketAttributes);
//                    try {
//                        TimerTask delegate = pinpointWebSocketTimerTaskDecorator.decorate(timerTask);
//                        delegate.run();
//                    } finally {
//                        WebSocketContextHolder.setAttributes(previousWebSocketAttributes);
//                    }
//                }
//            };
//        }
//    }
}
