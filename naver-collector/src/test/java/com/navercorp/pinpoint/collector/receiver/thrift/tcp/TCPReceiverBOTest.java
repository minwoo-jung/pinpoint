/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.thrift.tcp;

import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.rpc.packet.Packet;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import org.apache.thrift.TBase;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author koo.taejin
 */
@ContextConfiguration("classpath:applicationContext-collector-naver.xml")
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {"pinpoint.profiles.active=local"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TCPReceiverBOTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AtomicInteger requestId = new AtomicInteger(0);

    private Socket socket;
    private OutputStream os;
    private InputStream is;

    @Before
    public void setUp() throws Exception {
        this.socket = connectTcpReceiver();
        this.os = socket.getOutputStream();
        this.is = socket.getInputStream();
    }

    @After
    public void tearDown() throws Exception {
        IOUtils.closeQuietly(this.os);
        IOUtils.closeQuietly(this.is);
        if (socket != null) {
            socket.close();
        }
    }

    @Test(expected = RuntimeException.class)
    public void agentInfoTest1() throws Exception {
        TAgentInfo agentInfo = getAgentInfo();
        encodeAndWrite(os, agentInfo, false);
        ResponsePacket responsePacket = readAndDecode(is, 1000);
    }

    @Test
    public void agentInfoTest2() throws Exception {
        TAgentInfo agentInfo = getAgentInfo();
        encodeAndWrite(os, agentInfo, true);
        ResponsePacket responsePacket = readAndDecode(is, 3000);

        HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializerFactory().createDeserializer();
        byte[] payload = responsePacket.getPayload();
        Message<TBase<?, ?>> deserialize = deserializer.deserialize(payload);
        TResult result = (TResult) deserialize.getData();

        Assert.assertTrue(result.isSuccess());
    }

    private Socket connectTcpReceiver() throws IOException {
        InetSocketAddress endpoint = new InetSocketAddress("127.0.0.1", 9994);

        Socket socket = new Socket();
        socket.setSoTimeout(3000);
        socket.connect(endpoint);

        return socket;
    }

    private void encodeAndWrite(OutputStream os, TBase tbase, boolean isReqRes) throws Exception {
        HeaderTBaseSerializer serializer = HeaderTBaseSerializerFactory.DEFAULT_FACTORY.createSerializer();
        byte[] payload = serializer.serialize(tbase);

        Packet packet = null;
        if (isReqRes) {
            packet = new RequestPacket(nextRequestId(), payload);
        } else {
            packet = new SendPacket(payload);
        }

        os.write(packet.toBuffer().toByteBuffer().array());
        os.flush();
    }

    private int nextRequestId() {
        return requestId.incrementAndGet();
    }

    private ResponsePacket readAndDecode(InputStream is, long waitTimeMillis) throws Exception {
        final long startTimeMillis = System.currentTimeMillis();

        while (true) {
            int avaiableRead = is.available();

            if (avaiableRead > 0) {
                byte[] payload = new byte[avaiableRead];
                is.read(payload);

                for (byte b : payload) {
                    logger.warn("!!!{}", b);
                }

                ChannelBuffer cb = ChannelBuffers.wrappedBuffer(payload);
                cb.readByte();
                cb.readByte();


                ResponsePacket responsePacket = ResponsePacket.readBuffer((short) 6, cb);
                return responsePacket;
            }

            final long executionTime = System.currentTimeMillis() - startTimeMillis;
            if (waitTimeMillis < executionTime) {
                throw new RuntimeException("timeout " + executionTime + "ms");
            }
        }
    }

    private TAgentInfo getAgentInfo() {
        TAgentInfo agentInfo = new TAgentInfo("hostname", "127.0.0.1", "8081", "agentId", "appName", (short) 2, 1111, "1", "1.7.0_60", System.currentTimeMillis());
        return agentInfo;
    }

}
