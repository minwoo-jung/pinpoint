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
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.flink.Bootstrap;
import com.navercorp.pinpoint.flink.StatStreamingVer2Job;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.thrift.TBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author minwoo.jung
 */
public class TcpSourceFunction implements SourceFunction<TBase> {

    private TCPReceiver tcpReceiver;

    @Override
    public void run(SourceContext<TBase> ctx) throws Exception {
        //TODO : (minwoo) Bootstrap 에서 application context 안가져오고 바로 가져올수 있도록 개선;
        ApplicationContext appCtx = Bootstrap.getInstance().getApplicationContext();
        CollectorConfiguration configuration = appCtx.getBean("collectorConfiguration", CollectorConfiguration.class);
        DispatchHandler tcpDispatchHandlerWrapper = appCtx.getBean("tcpDispatchHandlerWrapper", DispatchHandler.class);
        PinpointServerAcceptor serverAcceptor = appCtx.getBean("serverAcceptor", PinpointServerAcceptor.class);
        TCPReceiver tcpReceiver = new TCPReceiver(configuration, tcpDispatchHandlerWrapper, serverAcceptor, ctx);
        tcpReceiver.afterPropertiesSet();
         tcpReceiver.start();
        Thread.sleep(Long.MAX_VALUE);
    }

    @Override
    public void cancel() {
        //tcpReceiver 정리 작업
    }
}