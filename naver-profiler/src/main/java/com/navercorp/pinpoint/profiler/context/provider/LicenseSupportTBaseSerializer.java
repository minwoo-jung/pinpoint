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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import com.navercorp.pinpoint.security.SecurityConstants;
import com.navercorp.pinpoint.thrift.io.TBaseSerializer;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LicenseSupportTBaseSerializer implements TBaseSerializer {

    private final TBaseSerializer tBaseSerializer;

    private final HeaderEntity headerEntity;

    public LicenseSupportTBaseSerializer(TBaseSerializer tBaseSerializer, String licenseKey) {
        this.tBaseSerializer = Assert.requireNonNull(tBaseSerializer, "tBaseSerializer must not be null");

        Assert.requireNonNull(licenseKey, "licenseKey must not be null");
        this.headerEntity = newLicenseHeader(licenseKey);
    }

    @Override
    public byte[] serialize(TBase<?, ?> base) throws TException {
        return tBaseSerializer.serialize(base, headerEntity);
    }

    @Override
    public byte[] serialize(TBase<?, ?> base, HeaderEntity headerEntity) throws TException {
        HeaderEntity newHeader = addLicenseKey(headerEntity);
        return tBaseSerializer.serialize(base, newHeader);
    }

    private HeaderEntity addLicenseKey(HeaderEntity headerEntity) {
        final Map<String, String> original = headerEntity.getEntityAll();

        final Map<String, String> copy = new HashMap<String, String>(original);
        copy.putAll(headerEntity.getEntityAll());

        return new HeaderEntity(copy);
    }


    private HeaderEntity newLicenseHeader(String licenseKey) {
        Assert.requireNonNull(licenseKey, "licenseKey must not be null");

        Map<String, String> headerEntityData = new HashMap<String, String>(1);
        headerEntityData.put(SecurityConstants.KEY_LICENSE_KEY, licenseKey);

        HeaderEntity headerEntity = new HeaderEntity(headerEntityData);
        return headerEntity;
    }
}
