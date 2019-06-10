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

package com.navercorp.pinpoint.collector.receiver.thrift.tcp.security.token;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.TCPPacketHandler;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.TCPPacketHandlerFactory;
import com.navercorp.pinpoint.collector.service.NamespaceService;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Profile("tokenAuthentication")
public class TokenEnableTCPPacketHandlerFactory implements TCPPacketHandlerFactory {

    private final SerializerFactory<HeaderTBaseSerializer> serializerFactory;
    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;

    private final NamespaceService namespaceService;

    public TokenEnableTCPPacketHandlerFactory(SerializerFactory<HeaderTBaseSerializer> serializerFactory,
                                              DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory,
                                              NamespaceService namespaceService) {
        this.serializerFactory = Objects.requireNonNull(serializerFactory, "serializerFactory must not be null");
        this.deserializerFactory = Objects.requireNonNull(deserializerFactory, "deserializerFactory must not be null");
        this.namespaceService = Objects.requireNonNull(namespaceService, "namespaceService must not be null");
    }


    @Override
    public TCPPacketHandler build(DispatchHandler dispatchHandler) {
        Objects.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
        return new TokenEnableTCPPacketHandler(dispatchHandler, serializerFactory, deserializerFactory, namespaceService);
    }

}
