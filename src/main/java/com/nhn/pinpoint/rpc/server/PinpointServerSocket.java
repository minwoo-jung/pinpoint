package com.nhn.pinpoint.rpc.server;

import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.client.WriteFailFutureListener;
import com.nhn.pinpoint.rpc.packet.*;
import com.nhn.pinpoint.rpc.util.CpuUtils;
import com.nhn.pinpoint.rpc.util.TimerFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.ChannelGroupFutureListener;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerBossPool;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class PinpointServerSocket extends SimpleChannelHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    private final boolean isDebug = logger.isDebugEnabled();

    private static final int WORKER_COUNT = CpuUtils.workerCount();

    private volatile boolean released;
    private ServerBootstrap bootstrap;

    private Channel serverChannel;
    private final ChannelGroup channelGroup = new DefaultChannelGroup();

    private final Timer pingTimer;

    private ServerMessageListener messageListener = SimpleLoggingServerMessageListener.LISTENER;
    private WriteFailFutureListener traceSendAckWriteFailFutureListener = new  WriteFailFutureListener(logger, "TraceSendAckPacket send fail.");

    public PinpointServerSocket() {
        ServerBootstrap bootstrap = createBootStrap(1, WORKER_COUNT);
        setOptions(bootstrap);
        addPipeline(bootstrap);
        this.bootstrap = bootstrap;
        this.pingTimer = TimerFactory.createHashedWheelTimer("PinpointServerSocket-PingTimer", 50, TimeUnit.MILLISECONDS, 512);
    }

    private void addPipeline(ServerBootstrap bootstrap) {
        ServerPipelineFactory serverPipelineFactory = new ServerPipelineFactory(this);
        bootstrap.setPipelineFactory(serverPipelineFactory);
    }

    void setPipelineFactory(ChannelPipelineFactory channelPipelineFactory) {
        if (channelPipelineFactory == null) {
            throw new NullPointerException("channelPipelineFactory must not be null");
        }
        bootstrap.setPipelineFactory(channelPipelineFactory);
    }

    public void setMessageListener(ServerMessageListener messageListener) {
        if (messageListener == null) {
            throw new NullPointerException("messageListener must not be null");
        }
        this.messageListener = messageListener;
    }

    private void setOptions(ServerBootstrap bootstrap) {
        // read write timeout이 있어야 되나? nio라서 없어도 되던가?

        // tcp 세팅
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        // buffer
        bootstrap.setOption("child.sendBufferSize", 1024 * 64);
        bootstrap.setOption("child.receiveBufferSize", 1024 * 64);

//        bootstrap.setOption("child.soLinger", 0);


    }


    private ServerBootstrap createBootStrap(int bossCount, int workerCount) {
        // profiler, collector,
        ExecutorService boss = Executors.newCachedThreadPool(new PinpointThreadFactory("Pinpoint-Server-Boss"));
        NioServerBossPool nioServerBossPool = new NioServerBossPool(boss, bossCount, ThreadNameDeterminer.CURRENT);

        ExecutorService worker = Executors.newCachedThreadPool(new PinpointThreadFactory("Pinpoint-Server-Worker"));
        NioWorkerPool nioWorkerPool = new NioWorkerPool(worker, workerCount, ThreadNameDeterminer.CURRENT);

        NioServerSocketChannelFactory nioClientSocketChannelFactory = new NioServerSocketChannelFactory(nioServerBossPool, nioWorkerPool);
        return new ServerBootstrap(nioClientSocketChannelFactory);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object message = e.getMessage();
        if (message instanceof Packet) {
            final Packet packet = (Packet) message;
            final short packetType = packet.getPacketType();
            final Channel channel = e.getChannel();
            logger.debug("messageReceived:{} channel:{}", message, channel);
            switch (packetType) {
                case PacketType.APPLICATION_SEND: {
                    SocketChannel socketChannel = getChannelContext(channel).getSocketChannel();
                    messageListener.handleSend((SendPacket) message, socketChannel);
                    return;
                }
//                case PacketType.APPLICATION_TRACE_SEND: {
//                    SocketChannel socketChannel = getChannelContext(channel).getSocketChannel();
//                    TraceSendPacket traceSendPacket = (TraceSendPacket) message;
//                    try {
//                        messageListener.handleSend(traceSendPacket, socketChannel);
//                    } finally {
//                        TraceSendAckPacket traceSendAckPacket = new TraceSendAckPacket(traceSendPacket.getTransactionId());
//                        ChannelFuture write = channel.write(traceSendAckPacket);
//                        write.addListener(traceSendAckWriteFailFutureListener);
//                    }
//                    return;
//                }
                case PacketType.APPLICATION_REQUEST: {
                    SocketChannel socketChannel = getChannelContext(channel).getSocketChannel();
                    messageListener.handleRequest((RequestPacket) message, socketChannel);
                    return;
                }
                case PacketType.APPLICATION_STREAM_CREATE:
                case PacketType.APPLICATION_STREAM_CLOSE:
                case PacketType.APPLICATION_STREAM_CREATE_SUCCESS:
                case PacketType.APPLICATION_STREAM_CREATE_FAIL:
                case PacketType.APPLICATION_STREAM_RESPONSE:
                    handleStreamPacket((StreamPacket) message, channel);
                    return;

                case PacketType.CONTROL_CLIENT_CLOSE: {
                    closeChannel(channel);
                    return;
                }
                default:
                    logger.warn("invalid messageReceived msg:{}, connection:{}", message, e.getChannel());
            }
        } else {
            logger.warn("invalid messageReceived msg:{}, connection:{}", message, e.getChannel());
        }
    }

    private void closeChannel(Channel channel) {
        logger.debug("received ClientClosePacket {}", channel);
        ChannelContext channelContext = getChannelContext(channel);
        channelContext.closePacketReceived();
//      상대방이 닫는거에 반응해서 socket을 닫도록 하자.
//        channel.close();
    }

    private void handleStreamPacket(StreamPacket packet, Channel channel) {
        ChannelContext context = getChannelContext(channel);
        if (packet instanceof StreamCreatePacket) {
            logger.debug("StreamCreate {}, streamId:{}", channel, packet.getChannelId());
            try {
                ServerStreamChannel streamChannel = context.createStreamChannel(packet.getChannelId());
                boolean success = streamChannel.receiveChannelCreate((StreamCreatePacket) packet);
                if (success) {
                    messageListener.handleStream(packet, streamChannel);
                }

            } catch (PinpointSocketException e) {
                logger.warn("channel create fail. channel:{} Caused:{}", channel, e);
            }
        } else if (packet instanceof StreamClosePacket) {
            logger.debug("StreamDestroy {}, streamId:{}", channel, packet.getChannelId());
            ServerStreamChannel streamChannel = context.getStreamChannel(packet.getChannelId());
            boolean close = streamChannel.close();
            if (close) {
                messageListener.handleStream(packet, streamChannel);
            } else {
                logger.warn("invalid streamClosePacket. already close. channel:{} Caused:{}", channel);
            }
        } else {
            logger.warn("invalid streamPacket. channel:{}", channel);
        }
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        final Channel channel = e.getChannel();
        if (logger.isDebugEnabled()) {
            logger.debug("server channelOpen {}", channel);
        }
        super.channelOpen(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        final Channel channel = e.getChannel();
        if (logger.isDebugEnabled()) {
            logger.debug("server channelConnected {}", channel);
        }
        prepareChannel(channel);
        super.channelConnected(ctx, e);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        final Channel channel = e.getChannel();
        if (logger.isDebugEnabled()) {
            logger.debug("server channelConnected {}", channel);
        }
        this.channelGroup.remove(channel);
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        final Channel channel = e.getChannel();
        final ChannelContext channelContext = getChannelContext(channel);
        if (channelContext.isClosePacketReceived()) {
            if (logger.isDebugEnabled()) {
                logger.debug("client channelClosed. normal closed. {}", channel);
            }
        } else if(released) {
            if (logger.isDebugEnabled()) {
                logger.debug("client channelClosed. server shutdown. {}", channel);
            }
        } else {
            logger.warn("Unexpected Client channelClosed {}", channel);

        }
        channelContext.closeAllStreamChannel();
    }

    private ChannelContext getChannelContext(Channel channel) {
        return (ChannelContext) channel.getAttachment();
    }

    private void prepareChannel(Channel channel) {
        ChannelContext channelContext = new ChannelContext(channel);

        channel.setAttachment(channelContext);

        channelGroup.add(channel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("Unexpected Exception happened. event:{}", e, e.getCause());
        Channel channel = e.getChannel();
        channel.close();
    }


    public void bind(String host, int port) throws PinpointSocketException {
        if (released) {
            return;
        }

        InetSocketAddress address = new InetSocketAddress(host, port);
        this.serverChannel = bootstrap.bind(address);
        sendPing();
    }

    private void sendPing() {
        logger.debug("sendPing");
        final TimerTask pintTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                if (timeout.isCancelled() || timeout.isExpired()) {
                    newPingTimeout(this);
                    return;
                }

                final ChannelGroupFuture write = channelGroup.write(PingPacket.PING_PACKET);
                write.addListener(new ChannelGroupFutureListener() {
                    @Override
                    public void operationComplete(ChannelGroupFuture future) throws Exception {
                        if (logger.isDebugEnabled()) {
                            for (ChannelFuture channelFuture : future) {
                                if (!channelFuture.isDone()) {
                                    final Throwable cause = channelFuture.getCause();
                                    logger.debug("ping write fail channel:{} Caused:{}", channelFuture.getChannel(), cause.getMessage(), cause);
                                }
                            }
                        }
                    }
                });
                newPingTimeout(this);
            }
        };
        newPingTimeout(pintTask);
    }

    private void newPingTimeout(TimerTask pintTask) {
        try {
            logger.debug("newPingTimeout");
            pingTimer.newTimeout(pintTask, 1000 * 60 * 5, TimeUnit.MILLISECONDS);
        } catch (IllegalStateException e) {
            // timer가 stop일 경우 정지.
            logger.debug("timer stopped. Caused:{}", e.getMessage());
        }
    }


    public void close() {
        synchronized (this) {
            if (released) {
                return;
            }
            released = true;
        }

        pingTimer.stop();
        if (serverChannel != null) {
            ChannelFuture close = serverChannel.close();
            close.awaitUninterruptibly(3000, TimeUnit.MILLISECONDS);
            serverChannel = null;
        }
        if (bootstrap != null) {
            bootstrap.releaseExternalResources();
            bootstrap = null;
        }
    }
}
