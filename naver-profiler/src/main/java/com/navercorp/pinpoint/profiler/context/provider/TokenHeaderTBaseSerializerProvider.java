/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.io.util.TypeLocator;
import com.navercorp.pinpoint.thrift.io.AuthenticationTBaseLocator;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import org.apache.thrift.TBase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class TokenHeaderTBaseSerializerProvider implements Provider<HeaderTBaseSerializer> {

    private final HeaderTBaseSerializerFactory headerTBaseSerializerFactory;

    @Inject
    public TokenHeaderTBaseSerializerProvider() {
        TypeLocator<TBase<?, ?>> tBaseLocator = AuthenticationTBaseLocator.getTypeLocator();

        this.headerTBaseSerializerFactory = new HeaderTBaseSerializerFactory(tBaseLocator);
    }

    @Override
    public HeaderTBaseSerializer get() {
        return headerTBaseSerializerFactory.createSerializer();
    }

}

