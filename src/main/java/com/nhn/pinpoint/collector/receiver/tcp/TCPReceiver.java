package com.nhn.pinpoint.collector.receiver.tcp;

import java.net.SocketAddress;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.nhn.pinpoint.collector.receiver.DispatchHandler;
import com.nhn.pinpoint.collector.util.PacketUtils;
import com.nhn.pinpoint.thrift.io.Header;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.common.util.ExecutorFactory;
import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.packet.StreamPacket;
import com.nhn.pinpoint.rpc.server.PinpointServerSocket;
import com.nhn.pinpoint.rpc.server.ServerMessageListener;
import com.nhn.pinpoint.rpc.server.ServerStreamChannel;
import com.nhn.pinpoint.rpc.server.SocketChannel;
import com.nhn.pinpoint.thrift.io.SafeHeaderTBaseSerializer;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class TCPReceiver {

	private final Logger logger = LoggerFactory.getLogger(TCPReceiver.class);

    private final ThreadFactory THREAD_FACTORY = new PinpointThreadFactory("Pinpoint-TCP-Worker");
	private final PinpointServerSocket pinpointServerSocket;
    private final DispatchHandler dispatchHandler;
    private final String bindAddress;
    private final int port;

    private int threadSize = 256;
    private int workerQueueSize = 1024 * 5;

    private final ThreadPoolExecutor worker = ExecutorFactory.newFixedThreadPool(threadSize, workerQueueSize, THREAD_FACTORY);

    public TCPReceiver(DispatchHandler dispatchHandler, String bindAddress, int port) {
        if (dispatchHandler == null) {
            throw new NullPointerException("dispatchHandler must not be null");
        }
        if (bindAddress == null) {
            throw new NullPointerException("bindAddress must not be null");
        }
        this.pinpointServerSocket = new PinpointServerSocket();
        this.dispatchHandler = dispatchHandler;
        this.bindAddress = bindAddress;
        this.port = port;
	}

    @PostConstruct
	public void start() {
        // message handler를 붙일 경우 주의점
        // iothread에서 올라오는 이벤트 이기 때문에. queue에 넣던가. 별도 thread처리등을 해야 한다.
        this.pinpointServerSocket.setMessageListener(new ServerMessageListener() {
            @Override
            public void handleSend(SendPacket sendPacket, SocketChannel channel) {
                receive(sendPacket, channel);
            }

            @Override
            public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
                requestResponse(requestPacket, channel);
            }

            @Override
            public void handleStream(StreamPacket streamPacket, ServerStreamChannel streamChannel) {
                logger.warn("unsupported streamPacket received {}", streamPacket);
            }
        });
        this.pinpointServerSocket.bind(bindAddress, port);


	}

    private void receive(SendPacket sendPacket, SocketChannel channel) {
        try {
            worker.execute(new Dispatch(sendPacket.getPayload(), channel.getRemoteAddress()));
        } catch (RejectedExecutionException e) {
            // 이건 stack trace찍어 봤자임. 원인이 명확함. 어떤 메시지 에러인지 좀더 알기 쉽게 찍을 필요성이 있음.
            logger.warn("RejectedExecutionException Caused:{}", e.getMessage());
        }
    }

    private void requestResponse(RequestPacket requestPacket, SocketChannel channel) {
        try {
            worker.execute(new RequestResponseDispatch(requestPacket, channel));
        } catch (RejectedExecutionException e) {
            // 이건 stack trace찍어 봤자임. 원인이 명확함. 어떤 메시지 에러인지 좀더 알기 쉽게 찍을 필요성이 있음.
            logger.warn("RejectedExecutionException Caused:{}", e.getMessage());
        }
    }

    private class Dispatch implements Runnable {
        private final byte[] bytes;
        private final SocketAddress remoteAddress;


        private Dispatch(byte[] bytes, SocketAddress remoteAddress) {
            if (bytes == null) {
                throw new NullPointerException("bytes");
            }
            this.bytes = bytes;
            this.remoteAddress = remoteAddress;
        }

        @Override
        public void run() {
            final HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializer();
            try {
                TBase<?, ?> tBase = deserializer.deserialize(bytes);
                dispatchHandler.dispatch(tBase, bytes, Header.HEADER_SIZE, bytes.length);
            } catch (TException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("packet serialize error. SendSocketAddress:{} Cause:{}", remoteAddress, e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpByteArray(bytes));
                }
            } catch (Exception e) {
                // 잘못된 header가 도착할 경우 발생하는 케이스가 있음.
                if (logger.isWarnEnabled()) {
                    logger.warn("Unexpected error. SendSocketAddress:{} Cause:{}", remoteAddress, e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpByteArray(bytes));
                }
            }
        }
    }

    private class RequestResponseDispatch implements Runnable {
        private final RequestPacket requestPacket;
        private final SocketChannel socketChannel;


        private RequestResponseDispatch(RequestPacket requestPacket, SocketChannel socketChannel) {
            if (requestPacket == null) {
                throw new NullPointerException("requestPacket");
            }
            this.requestPacket = requestPacket;
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {
            final HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializer();
            byte[] bytes = requestPacket.getPayload();
            SocketAddress remoteAddress = socketChannel.getRemoteAddress();
            try {
                TBase<?, ?> tBase = deserializer.deserialize(bytes);
                TBase result = dispatchHandler.dispatch(tBase, bytes, Header.HEADER_SIZE, bytes.length);
                if (result != null) {
                    SafeHeaderTBaseSerializer serializer = new SafeHeaderTBaseSerializer();
                    byte[] resultBytes = serializer.serialize(result);
                    socketChannel.sendResponseMessage(requestPacket, resultBytes);
                }
            } catch (TException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("packet serialize error. SendSocketAddress:{} Cause:{}", remoteAddress, e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpByteArray(bytes));
                }
            } catch (Exception e) {
                // 잘못된 header가 도착할 경우 발생하는 케이스가 있음.
                if (logger.isWarnEnabled()) {
                    logger.warn("Unexpected error. SendSocketAddress:{} Cause:{}", remoteAddress, e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpByteArray(bytes));
                }
            }
        }
    }

    @PreDestroy
    public void stop() {
        logger.info("Pinpoint-TCP-Server stop");
        pinpointServerSocket.close();
        worker.shutdown();
        try {
            worker.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
