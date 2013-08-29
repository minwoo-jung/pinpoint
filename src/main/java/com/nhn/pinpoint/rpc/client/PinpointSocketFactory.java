package com.nhn.pinpoint.rpc.client;


import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientBossPool;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class PinpointSocketFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String CONNECT_TIMEOUT_MILLIS = "connectTimeoutMillis";
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    private volatile boolean released;
    private ClientBootstrap bootstrap;

    private long reconnectDelay = 3 * 1000;
    private Timer timer;
    // ping은 1분 주기
    private long pingDelay = 60 * 1000;
    private long timeoutMillis = 3 * 1000;


    public PinpointSocketFactory() {
        this(1, 1);
    }

    public PinpointSocketFactory(int bossCount, int workerCount) {
        if (bossCount < 1) {
            throw new IllegalArgumentException("bossCount is negative: " + bossCount);
        }
        // timer를 connect timeout으로 쓰므로 먼저 만들어야 됨.
        Timer timer = createTimer();
        ClientBootstrap bootstrap = createBootStrap(bossCount, workerCount, timer);
        setOptions(bootstrap);
        addPipeline(bootstrap);

        this.bootstrap = bootstrap;
        this.timer = timer;
    }

    private Timer createTimer() {
        ThreadFactory threadFactory = new PinpointThreadFactory("Pinpoint-SocketFactory-Timer", true);
        HashedWheelTimer timer = new HashedWheelTimer(threadFactory, ThreadNameDeterminer.CURRENT, 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        return timer;
    }

    private void addPipeline(ClientBootstrap bootstrap) {
        SocketClientPipelineFactory socketClientPipelineFactory = new SocketClientPipelineFactory(this);
        bootstrap.setPipelineFactory(socketClientPipelineFactory);
    }

    private void setOptions(ClientBootstrap bootstrap) {
        // connectTimeout
        bootstrap.setOption(CONNECT_TIMEOUT_MILLIS, DEFAULT_CONNECT_TIMEOUT);
        // read write timeout이 있어야 되나? nio라서 없어도 되던가?

        // tcp 세팅
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        // buffer
        bootstrap.setOption("sendBufferSize", 1024 * 64);
        bootstrap.setOption("receiveBufferSize", 1024 * 64);

    }

    public void setConnectTimeout(int connectTimeout) {
        if (connectTimeout < 0) {
            throw new IllegalArgumentException("connectTimeout cannot be a negative number");
        }
        bootstrap.setOption(CONNECT_TIMEOUT_MILLIS, connectTimeout);
    }

    public int getConnectTimeout() {
        return (Integer) bootstrap.getOption(CONNECT_TIMEOUT_MILLIS);
    }

    public long getReconnectDelay() {
        return reconnectDelay;
    }

    public void setReconnectDelay(long reconnectDelay) {
        if (reconnectDelay < 0) {
            throw new IllegalArgumentException("reconnectDelay cannot be a negative number");
        }
        this.reconnectDelay = reconnectDelay;
    }

    public long getPingDelay() {
        return pingDelay;
    }

    public void setPingDelay(long pingDelay) {
        if (pingDelay < 0) {
            throw new IllegalArgumentException("pingDelay cannot be a negative number");
        }
        this.pingDelay = pingDelay;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        if (timeoutMillis < 0) {
            throw new IllegalArgumentException("timeoutMillis cannot be a negative number");
        }
        this.timeoutMillis = timeoutMillis;
    }

    private ClientBootstrap createBootStrap(int bossCount, int workerCount, Timer timer) {
        // profiler, collector,
        logger.debug("createBootStrap boss:{}, worker:{}", bossCount, workerCount);
        NioClientSocketChannelFactory nioClientSocketChannelFactory = createChannelFactory(bossCount, workerCount, timer);
        return new ClientBootstrap(nioClientSocketChannelFactory);
    }

    private NioClientSocketChannelFactory createChannelFactory(int bossCount, int workerCount, Timer timer) {

        ExecutorService boss = Executors.newCachedThreadPool(new PinpointThreadFactory("Pinpoint-Client-Boss", true));
        NioClientBossPool bossPool = new NioClientBossPool(boss, bossCount, timer, ThreadNameDeterminer.CURRENT);

        ExecutorService worker = Executors.newCachedThreadPool(new PinpointThreadFactory("Pinpoint-Client-Worker", true));
        NioWorkerPool workerPool = new NioWorkerPool(worker, workerCount, ThreadNameDeterminer.CURRENT);
        return new NioClientSocketChannelFactory(bossPool, workerPool);
    }

    public PinpointSocket connect(String host, int port) throws PinpointSocketException {
        SocketAddress address = new InetSocketAddress(host, port);
        ChannelFuture connectFuture = bootstrap.connect(address);
        SocketHandler socketHandler = getSocketHandler(connectFuture, address);

        PinpointSocket pinpointSocket = new PinpointSocket(socketHandler);
        traceSocket(pinpointSocket);
        return pinpointSocket;
    }

    public PinpointSocket reconnect(String host, int port) throws PinpointSocketException {
        SocketAddress address = new InetSocketAddress(host, port);
        ChannelFuture connectFuture = bootstrap.connect(address);
        SocketHandler socketHandler = getSocketHandler(connectFuture, address);

        PinpointSocket pinpointSocket = new PinpointSocket(socketHandler);
        traceSocket(pinpointSocket);
        return pinpointSocket;
    }

    private void traceSocket(PinpointSocket pinpointSocket) {
        // socket을 닫지 않고 clsoe했을 경우의 추적 로직이 필요함
        // 예외 케이스 이므로 나중에 만들어도 될듯.
    }

    public PinpointSocket scheduledConnect(String host, int port) {
        PinpointSocket pinpointSocket = new PinpointSocket();
        SocketAddress address = new InetSocketAddress(host, port);
        reconnect(pinpointSocket, address);
        return pinpointSocket;
    }


    SocketHandler getSocketHandler(ChannelFuture connectFuture, SocketAddress address) {
        if (address == null) {
            throw new NullPointerException("address");
        }

        SocketHandler socketHandler = getSocketHandler(connectFuture.getChannel());
        socketHandler.setConnectSocketAddress(address);

        connectFuture.awaitUninterruptibly();
        if (!connectFuture.isSuccess()) {
            throw new PinpointSocketException("connect fail. " + address, connectFuture.getCause());
        }
        socketHandler.open();
        return socketHandler;
    }

    public ChannelFuture reconnect(final SocketAddress remoteAddress) {
        if (remoteAddress == null) {
            throw new NullPointerException("remoteAddress");
        }

        ChannelPipeline pipeline;
        final ClientBootstrap bootstrap = this.bootstrap;
        try {
            pipeline = bootstrap.getPipelineFactory().getPipeline();
        } catch (Exception e) {
            throw new ChannelPipelineException("Failed to initialize a pipeline.", e);
        }
        SocketHandler socketHandler = (PinpointSocketHandler) pipeline.getLast();
        socketHandler.initReconnect();


        // Set the options.
        Channel ch = bootstrap.getFactory().newChannel(pipeline);
        boolean success = false;
        try {
            ch.getConfig().setOptions(bootstrap.getOptions());
            success = true;
        } finally {
            if (!success) {
                ch.close();
            }
        }

        // Connect.
        return ch.connect(remoteAddress);
    }


    private SocketHandler getSocketHandler(Channel channel) {
        return (SocketHandler) channel.getPipeline().getLast();
    }

    void reconnect(final PinpointSocket pinpointSocket, final SocketAddress socketAddress) {
        ConnectEvent connectEvent = new ConnectEvent(pinpointSocket, socketAddress);
        timer.newTimeout(connectEvent, reconnectDelay, TimeUnit.MILLISECONDS);
    }


    private class ConnectEvent implements TimerTask {

        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final PinpointSocket pinpointSocket;
        private final SocketAddress socketAddress;

        private ConnectEvent(PinpointSocket pinpointSocket, SocketAddress socketAddress) {
            if (pinpointSocket == null) {
                throw new NullPointerException("pinpointSocket must not be null");
            }
            if (socketAddress == null) {
                throw new NullPointerException("socketAddress must not be null");
            }

            this.pinpointSocket = pinpointSocket;
            this.socketAddress = socketAddress;
        }

        @Override
        public void run(Timeout timeout) {
            if (timeout.isCancelled()) {
                return;
            }
            // 이벤트는 fire됬지만 close됬을 경우 reconnect를 시도 하지 않음.
            if (pinpointSocket.isClosed()) {
                logger.debug("pinpointSocket is already closed.");
                return;
            }

            logger.warn("try reconnect. connectAddress:{}", socketAddress);
            final ChannelFuture channelFuture = reconnect(socketAddress);
            Channel channel = channelFuture.getChannel();
            final SocketHandler socketHandler = getSocketHandler(channel);
            socketHandler.setConnectSocketAddress(socketAddress);
            socketHandler.setPinpointSocket(pinpointSocket);

            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        Channel channel = future.getChannel();
                        logger.warn("reconnect success {}, {}", socketAddress, channel);
                        socketHandler.open();
                        pinpointSocket.reconnectSocketHandler(socketHandler);
                    } else {
                         if (!pinpointSocket.isClosed()) {
//                              구지 여기서 안찍어도 exceptionCought에서 메시지가 발생하므로 생략
//                            if (logger.isWarnEnabled()) {
//                                Throwable cause = future.getCause();
//                                logger.warn("reconnect fail. {} Caused:{}", socketAddress, cause.getMessage());
//                            }
                            reconnect(pinpointSocket, socketAddress);
                        } else {
                            logger.info("pinpointSocket is closed. stop reconnect.");
                        }
                    }
                }
            });
        }
    }


    public void release() {
        synchronized (this) {
            if (released) {
                return;
            }
            released = true;
        }

        if (bootstrap != null) {
            bootstrap.releaseExternalResources();
        }
        Set<Timeout> stop = this.timer.stop();
//        stop 뭔가 취소를 해야 되나??
    }
}
