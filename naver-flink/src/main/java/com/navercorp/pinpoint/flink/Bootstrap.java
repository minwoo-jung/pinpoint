/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.flink;

import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.join.CpuLoadSerializer;
import com.navercorp.pinpoint.flink.config.FlinkConfiguration;
import com.navercorp.pinpoint.flink.dao.hbase.StatisticsDao;
import com.navercorp.pinpoint.flink.process.TbaseFlatMapper;
import com.navercorp.pinpoint.flink.receiver.TcpSourceFunction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author minwoo.jung
 */
public class Bootstrap {

    private final static Bootstrap INSTANCE = new Bootstrap();

    private final StatisticsDao statisticsDao;
    private final ApplicationContext applicationContext;
    private final TcpSourceFunction tcpSourceFunction;
    private final TbaseFlatMapper tbaseFlatMapper;
    private final FlinkConfiguration flinkConfiguration;

    private Bootstrap() {
        String[] SPRING_CONFIG_XML = new String[]{"applicationContext-collector.xml", "applicationContext-cache.xml"};
        applicationContext = new ClassPathXmlApplicationContext(SPRING_CONFIG_XML);
        tbaseFlatMapper = applicationContext.getBean("tbaseFlatMapper", TbaseFlatMapper.class);
        flinkConfiguration = applicationContext.getBean("flinkConfiguration", FlinkConfiguration.class);

        final HbaseTemplate2 hbaseTemplate2 = applicationContext.getBean("hbaseTemplate", HbaseTemplate2.class);
        final ApplicationStatHbaseOperationFactory ApplicationStatHbaseOperationFactory = applicationContext.getBean("applicationStatHbaseOperationFactory", ApplicationStatHbaseOperationFactory.class);
        final CpuLoadSerializer cpuLoadSerializer = applicationContext.getBean("cpuLoadSerializer", CpuLoadSerializer.class);

        //TODO : (minwoo) 아래 두객체도 spring 전환 필요함.
        statisticsDao = new StatisticsDao(hbaseTemplate2, ApplicationStatHbaseOperationFactory, cpuLoadSerializer);

        tcpSourceFunction = new TcpSourceFunction();

    }

    public static Bootstrap getInstance() {
        return INSTANCE;
    }

    public StatisticsDao getStatisticsDao() {
        return statisticsDao;
    }

    public TcpSourceFunction getTcpFuncation() {
        return tcpSourceFunction;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public TbaseFlatMapper getTbaseFlatMapper() {
        return tbaseFlatMapper;
    }

    public FlinkConfiguration getFlinkConfiguration() {
        return flinkConfiguration;
    }


}
