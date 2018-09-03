/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.rpc;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.io.request.EmptyMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.security.util.AuthenticationStateContext;
import com.navercorp.pinpoint.security.util.ExpiredTaskManager;
import com.navercorp.pinpoint.thrift.dto.command.TCmdAuthenticationToken;
import com.navercorp.pinpoint.thrift.dto.command.TCmdAuthenticationTokenRes;
import com.navercorp.pinpoint.thrift.dto.command.TTokenResponseCode;
import com.navercorp.pinpoint.thrift.io.AuthenticationTBaseLocator;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.ThreadLocalHeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class AuthenticationTokenHandler extends SimpleChannelHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthenticationTokenHandler.class);

    private final AuthenticationStateContext state = new AuthenticationStateContext();

    private final byte[] tokenPayload;

    private final ExpiredTaskManager<Channel> expiredManager;

    private final SerializerFactory<HeaderTBaseSerializer> serializerFactory;
    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;

    public AuthenticationTokenHandler(byte[] tokenPayload, ExpiredTaskManager<Channel> expiredManager) {
        this.tokenPayload = Assert.requireNonNull(tokenPayload, "tokenPayload must not be null");
        this.expiredManager = Assert.requireNonNull(expiredManager, "expiredManager must not be null");
        this.serializerFactory = createSerializerFactory();
        this.deserializerFactory = createDeserializerFactory();
    }

    private SerializerFactory<HeaderTBaseSerializer> createSerializerFactory() {
        final SerializerFactory<HeaderTBaseSerializer> serializerFactory = new HeaderTBaseSerializerFactory(AuthenticationTBaseLocator.build());
        return new ThreadLocalHeaderTBaseSerializerFactory<HeaderTBaseSerializer>(serializerFactory);
    }

    private DeserializerFactory<HeaderTBaseDeserializer> createDeserializerFactory() {
        final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new HeaderTBaseDeserializerFactory(AuthenticationTBaseLocator.build());
        return new ThreadLocalHeaderTBaseDeserializerFactory<HeaderTBaseDeserializer>(deserializerFactory);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception {
        final Channel channel = ctx.getChannel();
        if (state.changeStateProgress()) {
            ChannelFuture future = Channels.future(channel);

            RequestPacket requestPacket = createRequestPacket(tokenPayload);
            ctx.sendDownstream(new DownstreamMessageEvent(channel, future, requestPacket, channel.getRemoteAddress()));

            AuthenticationCompleteFutureListener completeFutureListener = new AuthenticationCompleteFutureListener(ctx, event);
            expiredManager.reserve(ctx.getChannel(), completeFutureListener);
        } else {
            throw new IllegalArgumentException("illegal state");
        }
    }

    private RequestPacket createRequestPacket(byte[] tokenPayload) {
        TCmdAuthenticationToken token = new TCmdAuthenticationToken();
        token.setToken(tokenPayload);

        byte[] payload = SerializationUtils.serialize(token, serializerFactory, null);
        return new RequestPacket(0, payload);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
        if (state.isCompleted()) {
            handleCompleted(ctx, event);
        } else {
            if (state.isInProgress()) {
                handle(ctx, event);
            } else {
                if (state.isCompleted()) {
                    handleCompleted(ctx, event);
                } else {
                    // abnormal situation : is unable to send data before authentication completed.
                    LOGGER.warn("message will be discarded. cause:authentication is in progress.channel:{}, message:{}", event.getChannel(), event);
                }
            }
        }
    }

    private void handleCompleted(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
        if (state.isSucceeded()) {
            ctx.sendUpstream(event);
        } else {
            throw new IllegalArgumentException("illegal state");
        }
    }

    private void handle(ChannelHandlerContext ctx, MessageEvent event) {
        Channel channel = ctx.getChannel();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("handle() started. channel:{}", channel);
        }

        Object message = event.getMessage();
        if (!(message instanceof ResponsePacket)) {
            handleFail(ctx, "received unexpected message");
            return;
        }

        ResponsePacket responsePacket = (ResponsePacket) message;
        TCmdAuthenticationTokenRes authenticationTokenRes = getAuthenticationTokenRes(responsePacket);
        if (authenticationTokenRes == null) {
            handleFail(ctx, "received unexpected tBase");
            return;
        }

        TTokenResponseCode responseCode = authenticationTokenRes.getCode();
        if (responseCode == TTokenResponseCode.OK) {
            handleSuccess(ctx);
        } else {
            handleFail(ctx, responseCode.name());
        }
    }

    private TCmdAuthenticationTokenRes getAuthenticationTokenRes(ResponsePacket responsePacket) {
        final byte[] payload = responsePacket.getPayload();
        Message<TBase<?, ?>> message = SerializationUtils.deserialize(payload, deserializerFactory, EmptyMessage.INSTANCE);
        TBase<?, ?> messageData = message.getData();
        if (messageData instanceof TCmdAuthenticationTokenRes) {
            return (TCmdAuthenticationTokenRes) messageData;
        }
        return null;
    }

    private void handleSuccess(ChannelHandlerContext ctx) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("authentication success(). channel:{}", ctx.getChannel());
        }

        try {
            state.changeStateSuccess();
        } finally {
            ctx.getPipeline().remove(this);
            expiredManager.succeed(ctx.getChannel());
        }
    }

    private void handleFail(ChannelHandlerContext ctx, String failMessage) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("authentication fail. cause:{}, channel:{}", failMessage, ctx.getChannel());
        }

        try {
            state.changeStateFail();
        } finally {
            expiredManager.failed(ctx.getChannel());
            Channel channel = ctx.getChannel();
            if (channel != null) {
                channel.close();
            }
        }
    }

    public boolean isAuthenticated() {
        return state.isSucceeded();
    }


    private static class AuthenticationCompleteFutureListener implements FutureListener<Boolean> {

        private final ChannelHandlerContext channelHandlerContext;
        private final ChannelStateEvent channelConnectedEvent;

        public AuthenticationCompleteFutureListener(ChannelHandlerContext channelHandlerContext, ChannelStateEvent channelConnectedEvent) {
            this.channelHandlerContext = Assert.requireNonNull(channelHandlerContext, "channelHandlerContext must not be null");
            this.channelConnectedEvent = Assert.requireNonNull(channelConnectedEvent, "channelConnectedEvent must not be null");
        }

        @Override
        public void onComplete(Future<Boolean> future) {
            if (future.isSuccess()) {
                if (future.getResult()) {
                    channelHandlerContext.sendUpstream(channelConnectedEvent);
                    return;
                }
            }
            LOGGER.warn("authentication failed. cause: timeout expired");
            channelHandlerContext.getChannel().close();
        }

    }

}
