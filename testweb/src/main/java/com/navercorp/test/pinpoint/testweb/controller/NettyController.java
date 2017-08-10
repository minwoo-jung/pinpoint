/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.test.pinpoint.testweb.controller;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 해당 테스트를 위해서는 profiler.netty 설정을 활성화 하여야 합니다.
 *
 * @author Taejin Koo
 */
@Controller
public class NettyController {

    private static final String URL = "naver.com";
    private static final int DEFAULT_SLEEP_MILLIS = 20;
    private static final int DEFAULT_TIMEOUT = 10000;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/netty/listenerStyle")
    @ResponseBody
    public String netty4() throws Exception {
        CountDownLatch responseLatch = new CountDownLatch(1);

        Bootstrap bootstrap = client();
        try {
            ChannelFuture connectFuture = bootstrap.connect(URL, 80);
            connectFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    future.channel().pipeline().addLast(new SimpleChannelInboundHandler() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                            logger.info("message received. message:{}", msg);
                            responseLatch.countDown();
                        }
                    });

                    HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
                    future.channel().writeAndFlush(request);
                }
            });
        } finally {
        }

        boolean await = responseLatch.await(3000, TimeUnit.MILLISECONDS);
        if (await) {
            return "ok";
        } else {
            return "fail";
        }
    }

    @RequestMapping(value = "/netty/plainStyle")
    @ResponseBody
    public String netty5() throws Exception {
        CountDownLatch responseLatch = new CountDownLatch(1);

        Bootstrap bootstrap = client();

        try {
            Channel channel = bootstrap.connect(URL, 80).sync().channel();
            channel.pipeline().addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
                    logger.info("message received. message:{}", msg);
                    responseLatch.countDown();
                }
            });

            HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
            channel.writeAndFlush(request);
        } finally {
        }

        boolean await = responseLatch.await(3000, TimeUnit.MILLISECONDS);
        if (await) {
            return "ok";
        } else {
            return "fail";
        }
    }

    private Bootstrap client() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new HttpClientCodec());
                        ch.pipeline().addLast(new HttpObjectAggregator(65535));
                    }
                });
        return bootstrap;
    }

}
