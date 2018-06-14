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

package com.navercorp.pinpoint.collector.receiver.tcp.security.token;

import com.navercorp.pinpoint.thrift.io.AuthenticationTBaseLocator;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.io.TBaseLocator;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;

/**
 * @author Taejin Koo
 */
class TokenSerDes {

    private final TBaseLocator tBaseLocator = new AuthenticationTBaseLocator();

    private final SerializerFactory<HeaderTBaseSerializer> serializerFactory;
    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;

    TokenSerDes() {
        this.serializerFactory = createSerializerFactory();
        this.deserializerFactory = createDeserializerFactory();
    }

    private SerializerFactory<HeaderTBaseSerializer> createSerializerFactory() {
        final SerializerFactory<HeaderTBaseSerializer> serializerFactory = new HeaderTBaseSerializerFactory(tBaseLocator);
        return new ThreadLocalHeaderTBaseSerializerFactory<>(serializerFactory);
    }

    private DeserializerFactory<HeaderTBaseDeserializer> createDeserializerFactory() {
        final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new HeaderTBaseDeserializerFactory(tBaseLocator);
        return new ThreadLocalHeaderTBaseDeserializerFactory<>(deserializerFactory);
    }

    byte[] serialize(TBase object) {
        byte[] payload = SerializationUtils.serialize(object, serializerFactory, null);
        return payload;
    }

    <T extends TBase> T deserialize(byte[] payload, Class<T> type) {
        TBase tBase = SerializationUtils.deserialize(payload, deserializerFactory, null);
        if (tBase.getClass() == type) {
            return type.cast(tBase);
        } else {
            return null;
        }
    }

    TBaseLocator getTBaseLocator() {
        return tBaseLocator;
    }

}
