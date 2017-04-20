/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.flink.receiver;

import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.flink.Bootstrap;
import com.navercorp.pinpoint.flink.cluster.FlinkServerRegister;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author minwoo.jung
 */
//public class TcpSourceFunction implements SourceFunction<TBase> {
public class TcpSourceFunction implements ParallelSourceFunction<TBase> {

    private final Logger logger = LoggerFactory.getLogger(TcpSourceFunction.class);
    private TCPReceiver tcpReceiver = null;
    private ApplicationContext appCtx = null;

    @Override
    public void run(SourceContext<TBase> ctx) throws Exception {
        //TODO : (minwoo) Bootstrap 에서 application context 안가져오고 바로 가져올수 있도록 개선
        appCtx = Bootstrap.getInstance().getApplicationContext();
        AgentStatHandler agentStatHandler = new AgentStatHandler(ctx);
        TcpDispatchHandler tcpDispatchHandler = appCtx.getBean("tcpDispatchHandler", TcpDispatchHandler.class);
        tcpDispatchHandler.setAgentStatHandler(agentStatHandler);

        CollectorConfiguration configuration = appCtx.getBean("flinkConfiguration", CollectorConfiguration.class);
        DispatchHandler tcpDispatchHandlerWrapper = appCtx.getBean("tcpDispatchHandlerWrapper", DispatchHandler.class);
        PinpointServerAcceptor serverAcceptor = appCtx.getBean("serverAcceptor", PinpointServerAcceptor.class);
        appCtx.getBean("flinkServerRegister", FlinkServerRegister.class);
        tcpReceiver = new TCPReceiver(configuration, tcpDispatchHandlerWrapper, serverAcceptor, ctx);
        tcpReceiver.afterPropertiesSet();

        tcpReceiver.start();
        //TODO : (minwoo) sleep이 아니라 다른 형태로 대기 할수는 없는가?!
        Thread.sleep(Long.MAX_VALUE);
    }

    @Override
    public void cancel() {
        logger.info("cancel TcpSourceFunction.");
        if (tcpReceiver != null) {
            tcpReceiver.stop();
        }

        if (appCtx != null) {
            ((ConfigurableApplicationContext) appCtx).close();
        }
    }
}