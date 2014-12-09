package com.navercorp.pinpoint.web.alarm.collector;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.navercorp.pinpoint.common.ServiceType;
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
    
    @Test
    public void test() {
        Application application = new Application("API.GATEWAY.DEV", ServiceType.TOMCAT);
        long current = System.currentTimeMillis();
        Range range = new Range(current - 300000, current);
        LinkDataMap map = callerDao.selectCaller(application, range);

        for (LinkData linkData : map.getLinkDataList()) {
            System.out.println(linkData.getFromApplication() + " : " + linkData.getToApplication() );
            
            LinkCallDataMap linkCallDataMap = linkData.getLinkCallDataMap();
            for (LinkCallData linkCallData : linkCallDataMap.getLinkDataList()) {
                System.out.println("\t"+ linkCallData.getSource() + " : " + linkCallData.getTarget());
                for (TimeHistogram timeHistogram : linkCallData.getTimeHistogram()) {
                    System.out.println("\t\t" + timeHistogram);
                }
            }
            
            System.out.println(linkData.getLinkCallDataMap());
        }
    }
    
}
