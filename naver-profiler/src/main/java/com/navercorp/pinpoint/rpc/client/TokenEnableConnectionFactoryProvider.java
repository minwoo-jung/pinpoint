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

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.rpc.AuthenticationTokenHandler;
import com.navercorp.pinpoint.profiler.context.service.TokenService;
import com.navercorp.pinpoint.rpc.PipelineFactory;
import com.navercorp.pinpoint.security.util.ExpiredTaskManager;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.util.Timer;

/**
 * @author Taejin Koo
 */
public class TokenEnableConnectionFactoryProvider implements ConnectionFactoryProvider {

    private static final long DEFAULT_EXPIRY_TIME = 10000;

    private final PipelineFactory pipelineFactory;
    private final TokenService tokenService;

    public TokenEnableConnectionFactoryProvider(PipelineFactory pipelineFactory, TokenService tokenService) {
        this.pipelineFactory = Assert.requireNonNull(pipelineFactory, "pipelineFactory must not be null");
        this.tokenService = Assert.requireNonNull(tokenService, "tokenService must not be null");
    }

    @Override
    public ConnectionFactory get(Timer connectTimer, Closed closed, ChannelFactory channelFactory, SocketOption socketOption, ClientOption clientOption, ClientHandlerFactory clientHandlerFactory) {
        TokenEnablePipelineFactory tokenEnablePipelineFactory = new TokenEnablePipelineFactory(pipelineFactory, tokenService, connectTimer);
        return new ConnectionFactory(connectTimer, closed, channelFactory, socketOption, clientOption, clientHandlerFactory, tokenEnablePipelineFactory);
    }

    private final static class TokenEnablePipelineFactory implements PipelineFactory {

        private final PipelineFactory pipelineFactory;
        private final TokenService tokenService;
        private final Timer timer;

        public TokenEnablePipelineFactory(PipelineFactory pipelineFactory, TokenService tokenService, Timer timer) {
            this.pipelineFactory = Assert.requireNonNull(pipelineFactory, "pipelineFactory must not be null");
            this.tokenService = Assert.requireNonNull(tokenService, "tokenService must not be null");
            this.timer = Assert.requireNonNull(timer, "timer must not be null");
        }

        @Override
        public ChannelPipeline newPipeline() {
            ChannelPipeline pipeline = pipelineFactory.newPipeline();

            ExpiredTaskManager<Channel> expiredTaskManager = new ExpiredTaskManager<Channel>(timer, DEFAULT_EXPIRY_TIME);
            byte[] tokenPayload = tokenService.getToken("ALL");

            pipeline.addLast("token", new AuthenticationTokenHandler(tokenPayload, expiredTaskManager));

            return pipeline;
        }

    }


}
