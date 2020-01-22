/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.collector.etcd;

import com.navercorp.pinpoint.common.util.Assert;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class EtcdClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private static final long DEFAULT_OPERATION_TIMEOUT_MILLIS = 3000;

    private final DeleteOption DEFAULT_DELETE_OPTION = DeleteOption.newBuilder().withPrevKV(true).build();

    private final Client client;

    private final KV kvClient;
    private final Lease leaseClient;

    private final long operationTimeoutMillis;

    public EtcdClient(String address) {
        this(address, DEFAULT_OPERATION_TIMEOUT_MILLIS);
    }

    public EtcdClient(String address, long operationTimeoutMillis) {
        this.client = create(address);

        this.kvClient = client.getKVClient();
        this.leaseClient = client.getLeaseClient();

        Assert.isTrue(operationTimeoutMillis > 0, "operationTimeoutMillis > 0");
        this.operationTimeoutMillis = operationTimeoutMillis;
    }

    private Client create(String address) {
        Client client = Client.builder().endpoints(address).build();
        return client;
    }

    public void put(String key, byte[] value) throws Exception {
        put(key, value, -1);
    }

    public void put(String key, byte[] value, long ttl) throws Exception {
        ByteSequence keyByteSequence = ByteSequence.from(key, DEFAULT_CHARSET);
        ByteSequence valueByteSequence = ByteSequence.from(value);

        CompletableFuture<PutResponse> putFuture = kvClient.put(keyByteSequence, valueByteSequence, getPutOption(ttl));
        // Throw exception when message puts fail
        putFuture.get(operationTimeoutMillis, TimeUnit.MILLISECONDS);
    }

    private PutOption getPutOption(long ttl) throws Exception {
        if (ttl > 0) {
            long leaseId = getLeaseId(ttl);

            PutOption.Builder builder = PutOption.newBuilder();
            builder.withLeaseId(leaseId);
            return builder.build();
        } else {
            return PutOption.DEFAULT;
        }
    }

    private long getLeaseId(long ttl) throws Exception {
        CompletableFuture<LeaseGrantResponse> grant = leaseClient.grant(ttl);
        LeaseGrantResponse response = grant.get(operationTimeoutMillis, TimeUnit.MILLISECONDS);
        return response.getID();
    }


    public byte[] getData(String key) throws Exception {
        ByteSequence keyByteSequence = ByteSequence.from(key, DEFAULT_CHARSET);

        CompletableFuture<GetResponse> getResponseCompletableFuture = kvClient.get(keyByteSequence);
        GetResponse getResponse = getResponseCompletableFuture.get(operationTimeoutMillis, TimeUnit.MILLISECONDS);

        if (getResponse.getCount() == 0) {
            return null;
        }

        if (getResponse.getCount() > 1) {
            logger.warn("Failed to get data. Caused : response has multiple value");
            return null;
        }

        KeyValue keyValue = getResponse.getKvs().get(0);
        return keyValue.getValue().getBytes();
    }

    public Map<String, byte[]> getChildNodes(String prefix) throws Exception {
        ByteSequence prefixByteSequence = ByteSequence.from(prefix, DEFAULT_CHARSET);

        GetOption.Builder builder = GetOption.newBuilder();
        GetOption getOption = builder.withPrefix(prefixByteSequence).build();

        CompletableFuture<GetResponse> getResponseCompletableFuture = kvClient.get(prefixByteSequence, getOption);
        GetResponse getResponse = getResponseCompletableFuture.get(operationTimeoutMillis, TimeUnit.MILLISECONDS);

        if (getResponse.getCount() == 0) {
            return Collections.emptyMap();
        }

        Map<String, byte[]> result = new HashMap<>();
        for (KeyValue kv : getResponse.getKvs()) {
            ByteSequence keyByteSequence = kv.getKey();
            String key = keyByteSequence.toString(DEFAULT_CHARSET);

            ByteSequence valueByteSequence = kv.getValue();
            byte[] value = valueByteSequence.getBytes();

            result.put(key, value);
        }

        return result;
    }

    public byte[] delete(String key) throws Exception {
        ByteSequence keyByteSequence = ByteSequence.from(key, DEFAULT_CHARSET);

        CompletableFuture<DeleteResponse> delete = kvClient.delete(keyByteSequence, DEFAULT_DELETE_OPTION);
        DeleteResponse deleteResponse = delete.get(operationTimeoutMillis, TimeUnit.MILLISECONDS);
        List<KeyValue> prevKvs = deleteResponse.getPrevKvs();
        if (prevKvs.size() == 0) {
            return null;
        }

        if (prevKvs.size() > 1) {
            logger.warn("Failed to get data. Caused : response has multiple value");
            return null;
        }

        KeyValue keyValue = prevKvs.get(0);
        ByteSequence valueByteSequence = keyValue.getValue();
        return valueByteSequence.getBytes();
    }

    public void stop() {
        closeClientQuietly(leaseClient);
        closeClientQuietly(kvClient);
        closeClientQuietly(client);
    }

    private void closeClientQuietly(AutoCloseable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            // ignore
        }
    }

}
