package com.profiler.sender;

import com.profiler.common.dto.Header;
import com.profiler.common.util.DefaultTBaseLocator;
import com.profiler.common.util.HeaderTBaseSerializer;
import com.profiler.common.util.TBaseLocator;
import com.profiler.config.ProfilerConfig;
import com.profiler.context.Thriftable;
import com.profiler.util.Assert;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author netspider
 */
public class UdpDataSender implements DataSender, Runnable {

    private final Logger logger = Logger.getLogger(UdpDataSender.class.getName());

    private final LinkedBlockingQueue queue = new LinkedBlockingQueue(1024);

    private DatagramSocket udpSocket = null;
    private Thread ioThread;

    private TBaseLocator locator = new DefaultTBaseLocator();
    // 주의 single thread용임
    private HeaderTBaseSerializer serializer = new HeaderTBaseSerializer();

    private boolean started = false;

    private Object stopLock = new Object();

    public UdpDataSender(String host, int port) {
        Assert.notNull(host, "host must not be null");

        // Socket 생성에 에러가 발생하면 Agent start가 안되게 변경.
        this.udpSocket = createSocket(host, port);

        this.ioThread = createIoThread();

        this.started = true;
    }

    private Thread createIoThread() {
        Thread thread = new Thread(this);
        thread.setName("HIPPO-UdpDataSender:IoThread");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private DatagramSocket createSocket(String host, int port) {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(1000 * 5);

            InetSocketAddress serverAddress = new InetSocketAddress(host, port);
            datagramSocket.connect(serverAddress);
            return datagramSocket;
        } catch (SocketException e) {
            throw new IllegalStateException("DatagramSocket create fail. Cause" + e.getMessage(), e);
        }
    }

    public boolean send(TBase<?, ?> data) {
        if (!started) {
            return false;
        }
        // TODO: addedQueue가 full일 때 IllegalStateException처리.
        return queue.offer(data);
    }

    public boolean send(Thriftable thriftable) {
        if (!started) {
            return false;
        }
        // TODO: addedQueue가 full일 때 IllegalStateException처리.
        return queue.offer(thriftable);
    }

    @Override
    public void stop() {
        if (!started) {
            return;
        }
        started = false;
        // io thread 안전 종료. queue 비우기.
    }

    // TODO: sender thread가 한 개로 충분한가.
    public void run() {
        while (true) {
            try {
                Object dto = take();
                if (dto == null) {
                    continue;
                }
                send0(dto);
            } catch (Throwable e) {
                logger.log(Level.WARNING, "Unexpected Error Cause:" + e.getMessage(), e);
            }
        }
    }

    private void send0(Object dto) {
        TBase tBase;
        if (dto instanceof TBase) {
            tBase = (TBase) dto;
        } else if (dto instanceof Thriftable) {
            tBase = ((Thriftable) dto).toThrift();
        } else {
            logger.warning("invalid type:" + dto.getClass());
            return;
        }
        byte[] sendData = serialize(tBase);
        if (sendData == null) {
            logger.warning("sendData is null");
            return;
        }
        DatagramPacket packet = new DatagramPacket(sendData, sendData.length);
        try {
            udpSocket.send(packet);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Data sent. " + dto);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "packet send error " + dto, e);
        }
    }

    // TODO: addedqueue에서 bulk로 drain
    private Object take() {
        try {
            return queue.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    private byte[] serialize(TBase<?, ?> dto) {
        Header header = createHeader(dto);
        try {
            return serializer.serialize(header, dto);
        } catch (TException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Serialize fail:" + dto, e);
            }
            return null;
        }
    }

    private Header createHeader(TBase<?, ?> dto) {
        // TODO 구지 객체 생성을 안하고 정적 lookup이 가능할것 같음.
        short type = locator.typeLookup(dto);
        Header header = new Header();
        header.setType(type);
        return header;
    }
}
