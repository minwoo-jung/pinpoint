package com.nhn.pinpoint.collector.receiver.udp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import com.nhn.pinpoint.collector.receiver.DataReceiver;
import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;

import com.nhn.pinpoint.collector.spring.ApplicationContextUtils;

public class UDPReceiverTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
	public void startStop() {
		try {
			GenericApplicationContext context = ApplicationContextUtils.createContext();

			DataReceiver receiver = context.getBean("udpSpanReceiver", UDPReceiver.class);
//			receiver.start();
//            start 타이밍을 spring안으로 변경하였음.
            // start시점을 좀더 정확히 알수 있어야 될거 같음.
            // start한 다음에 바로 셧다운하니. receive thread에서 localaddress를 제대로 못찾는 문제가 있음.
            Thread.sleep(1000);

			receiver.shutdown();
			context.close();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

    @Test
    public void hostNullCheck() {
        InetSocketAddress address = new InetSocketAddress((InetAddress) null, 90);
        logger.debug(address.toString());
    }

    @Test
    public void socketBufferSize() throws SocketException {
        DatagramSocket datagramSocket = new DatagramSocket();
        int receiveBufferSize = datagramSocket.getReceiveBufferSize();
        System.out.println(receiveBufferSize);
        datagramSocket.setReceiveBufferSize(64*1024*10);
        System.out.println(datagramSocket.getReceiveBufferSize());
    }
}
