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

import com.navercorp.pinpoint.collector.receiver.thrift.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.TCPPacketHandler;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.TCPPacketHandlerFactory;
import com.navercorp.pinpoint.collector.service.MetadataService;
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

    private SerializerFactory<HeaderTBaseSerializer> serializerFactory;
    private DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;

    @Autowired
    private MetadataService metadataService;

    public TokenEnableTCPPacketHandlerFactory() {
    }

    public void setSerializerFactory(SerializerFactory<HeaderTBaseSerializer> serializerFactory) {
        this.serializerFactory = serializerFactory;
    }

    public void setDeserializerFactory(DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory) {
        this.deserializerFactory = deserializerFactory;
    }

    @Override
    public TCPPacketHandler build(DispatchHandler dispatchHandler) {
        Objects.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
        return new TokenEnableTCPPacketHandler(dispatchHandler, serializerFactory, deserializerFactory, metadataService);
    }

}
