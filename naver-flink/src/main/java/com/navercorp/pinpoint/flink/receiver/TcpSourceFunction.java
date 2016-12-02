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

import com.codahale.metrics.MetricRegistry;
import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.collector.mapper.thrift.stat.AgentStatBatchMapper;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author minwoo.jung
 */
public class TcpSourceFunction implements SourceFunction<String> {

    private static final String[] SPRING_CONFIG_XML = new String[] {"applicationContext-collector.xml"};

    private TCPReceiver tcpReceiver;

    @Override
    public void run(SourceContext<String> ctx) throws Exception {
        ApplicationContext appCtx = new ClassPathXmlApplicationContext(SPRING_CONFIG_XML);
        CollectorConfiguration configuration = appCtx.getBean("collectorConfiguration", CollectorConfiguration.class);
        DispatchHandler tcpDispatchHandlerWrapper = appCtx.getBean("tcpDispatchHandlerWrapper", DispatchHandler.class);
        PinpointServerAcceptor serverAcceptor = appCtx.getBean("serverAcceptor", PinpointServerAcceptor.class);
        AgentStatBatchMapper agentStatBatchMapper = appCtx.getBean("agentStatBatchMapper", AgentStatBatchMapper.class);
        TCPReceiver tcpReceiver = new TCPReceiver(configuration, tcpDispatchHandlerWrapper, serverAcceptor, ctx, agentStatBatchMapper);
        tcpReceiver.afterPropertiesSet();
         tcpReceiver.start();
        Thread.sleep(3000000L);
    }

    @Override
    public void cancel() {
        //tcpReceiver 정리 작업
    }
}