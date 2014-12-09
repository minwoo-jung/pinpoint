package com.navercorp.pinpoint.collector.receiver.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.thrift.TBase;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.navercorp.pinpoint.collector.receiver.TcpDispatchHandler;
import com.navercorp.pinpoint.collector.receiver.tcp.TCPReceiver;
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


/**
 * @author koo.taejin
 */
@ContextConfiguration("classpath:applicationContext-collector.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TCPReceiverBOTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	TcpDispatchHandler handler;

	@Autowired
	private TCPReceiver tcpReceiver;
	
	
	@Test
	public void agentInfoTest1() throws Exception {
		Socket socket = connectTcpReceiver();
		OutputStream os = socket.getOutputStream();
		InputStream is = socket.getInputStream();

		TAgentInfo agentInfo = getAgentInfo();
		encodeAndWrite(os, agentInfo, false);
		ResponsePacket responsePacket = readAndDecode(is, 1000);
		Assert.assertNull(responsePacket);

	}

	@Test
	public void agentInfoTest2() throws Exception {
		Socket socket = connectTcpReceiver();
		OutputStream os = socket.getOutputStream();
		InputStream is = socket.getInputStream();

		TAgentInfo agentInfo = getAgentInfo();
		encodeAndWrite(os, agentInfo, true);
		ResponsePacket responsePacket = readAndDecode(is, 1000);

        HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializerFactory().createDeserializer();
		TResult result = (TResult) deserializer.deserialize(responsePacket.getPayload());
		
		Assert.assertTrue(result.isSuccess());
	}

	private Socket connectTcpReceiver() throws IOException {
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress("127.0.0.1", 9994));

		return socket;
	}

	private void encodeAndWrite(OutputStream os, TBase tbase, boolean isReqRes) throws Exception {
		HeaderTBaseSerializer serializer = HeaderTBaseSerializerFactory.DEFAULT_FACTORY.createSerializer();
		byte[] payload = serializer.serialize(tbase);

		Packet packet = null;
		if (isReqRes) {
			packet = new RequestPacket(payload);
		} else {
			packet = new SendPacket(payload);
		}

		os.write(packet.toBuffer().toByteBuffer().array());
		os.flush();
	}

	private ResponsePacket readAndDecode(InputStream is, long waitTimeMillis) throws Exception {
		long startTimeMillis = System.currentTimeMillis();

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

			Thread.sleep(20);
			if (waitTimeMillis < System.currentTimeMillis() - startTimeMillis) {
				return null;
			}
		}
	}

	private TAgentInfo getAgentInfo() {
		TAgentInfo agentInfo = new TAgentInfo("hostname", "127.0.0.1", "8081", "agentId", "appName", (short) 2, 1111, "1", System.currentTimeMillis());
		return agentInfo;
	}

}
