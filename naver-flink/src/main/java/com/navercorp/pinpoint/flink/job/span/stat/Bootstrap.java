/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.flink.job.span.stat;

import com.navercorp.pinpoint.collector.receiver.thrift.TCPReceiverBean;
import com.navercorp.pinpoint.flink.cluster.FlinkServerRegister;
import com.navercorp.pinpoint.flink.config.FlinkConfiguration;
import com.navercorp.pinpoint.flink.job.span.stat.process.TBaseFlatMapper;
import com.navercorp.pinpoint.flink.job.span.stat.receiver.SpanStatHandler;
import com.navercorp.pinpoint.flink.job.span.stat.receiver.TcpDispatchHandler;
import com.navercorp.pinpoint.flink.job.span.stat.receiver.TcpSourceFunction;
import com.navercorp.pinpoint.flink.job.span.stat.service.SpanStatAgentService;
import com.navercorp.pinpoint.flink.job.span.stat.service.SpanStatService;
import com.navercorp.pinpoint.flink.vo.RawData;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;

/**
 * @author minwoo.jung
 */
public class Bootstrap {
    private static Logger logger = LoggerFactory.getLogger(Bootstrap.class);
    private static final String SPRING_PROFILE = "spring.profiles.active";

    private volatile static Bootstrap instance;

    private final ClassPathXmlApplicationContext applicationContext;
    private final TcpSourceFunction tcpSourceFunction;
    private final FlinkConfiguration flinkConfiguration;
    private final SpanStatHandler spanStatHandler;
    private final TcpDispatchHandler tcpDispatchHandler;
    private final TBaseFlatMapper tBaseFlatMapper;
    private final SpanStatService spanStatService;
    private final SpanStatAgentService spanStatAgentService;

    private Bootstrap() {
        applicationContext = new ClassPathXmlApplicationContext("job/span/applicationContext-span.xml");

        tcpSourceFunction = applicationContext.getBean("tcpSourceFunction", TcpSourceFunction.class);
        flinkConfiguration = applicationContext.getBean("flinkConfiguration", FlinkConfiguration.class);
        spanStatHandler = applicationContext.getBean("spanStatHandler", SpanStatHandler.class);
        tcpDispatchHandler = applicationContext.getBean("tcpDispatchHandler", TcpDispatchHandler.class);
        tBaseFlatMapper = applicationContext.getBean("tbaseFlatMapper", TBaseFlatMapper.class);
        spanStatService = applicationContext.getBean("spanStatService", SpanStatService.class);
        spanStatAgentService = applicationContext.getBean("spanStatAgentService", SpanStatAgentService.class);
    }

    public static Bootstrap getInstance(Map<String, String> jobParameters) {
        if (instance == null)  {
            synchronized(Bootstrap.class) {
                if (instance == null) {
                    String profiles = jobParameters.getOrDefault(SPRING_PROFILE, "local");
                    System.setProperty(SPRING_PROFILE, profiles);
                    instance = new Bootstrap();
                    logger.info("Bootstrap initialization. : job parameter " + jobParameters);
                }
            }
        }

        return instance;
    }

    public TcpSourceFunction getTcpSourceFunction() {
        return tcpSourceFunction;
    }

    public StreamExecutionEnvironment createStreamExecutionEnvironment() {
        if (flinkConfiguration.isLocalforFlinkStreamExecutionEnvironment()) {
            LocalStreamEnvironment localEnvironment = StreamExecutionEnvironment.createLocalEnvironment();
            localEnvironment.setParallelism(1);
            return localEnvironment;
        } else {
            return StreamExecutionEnvironment.getExecutionEnvironment();
        }
    }

    public void setStatHandlerTcpDispatchHandler(SourceContext<RawData> sourceContext) {
        spanStatHandler.addSourceContext(sourceContext);
        tcpDispatchHandler.setSimpletHandler(spanStatHandler);
    }

    public FlinkServerRegister initFlinkServerRegister() {
        return applicationContext.getBean("flinkServerRegister", FlinkServerRegister.class);
    }

    public void initTcpReceiver() {
        // lazy init
        applicationContext.getBean("tcpReceiver", TCPReceiverBean.class);
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public TBaseFlatMapper getTBaseFlatMapper() {
        return tBaseFlatMapper;
    }

    public SpanStatService getSpanStatService() {
        return spanStatService;
    }

    public SpanStatAgentService getSpanStatAgentService() {
        return spanStatAgentService;
    }
}
