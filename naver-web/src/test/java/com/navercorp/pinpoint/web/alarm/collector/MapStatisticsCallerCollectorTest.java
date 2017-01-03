package com.navercorp.pinpoint.web.alarm.collector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.dao.hbase.HbaseMapStatisticsCallerDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class MapStatisticsCallerCollectorTest {
    
    @Autowired
    HbaseMapStatisticsCallerDao callerDao;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void test() {
        Application application = new Application("API.GATEWAY.DEV", ServiceType.STAND_ALONE);
        long current = System.currentTimeMillis();
        Range range = new Range(current - 300000, current);
        LinkDataMap map = callerDao.selectCaller(application, range);

        for (LinkData linkData : map.getLinkDataList()) {
            logger.debug(linkData.getFromApplication() + " : " + linkData.getToApplication() );
            
            LinkCallDataMap linkCallDataMap = linkData.getLinkCallDataMap();
            for (LinkCallData linkCallData : linkCallDataMap.getLinkDataList()) {
                logger.debug("\t"+ linkCallData.getSource() + " : " + linkCallData.getTarget());
                for (TimeHistogram timeHistogram : linkCallData.getTimeHistogram()) {
                    logger.debug("\t\t" + timeHistogram);
                }
            }
            
            logger.debug(linkData.getLinkCallDataMap().toString());
        }
    }
    
}
