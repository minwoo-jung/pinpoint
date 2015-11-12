package com.navercorp.pinpoint.web.alarm;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.dao.MapResponseDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseTime;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class ProcessorTest {

    private static final String SERVICE_NAME = "local_tomcat";
    
    AlarmProcessor processor = new AlarmProcessor();
    
    private static MapResponseDao mockMapResponseDAO;
    
    @BeforeClass
    public static void before() {
        mockMapResponseDAO = new MapResponseDao() {
            
            @Override
            public List<ResponseTime> selectResponseTime(Application application, Range range) {
                List<ResponseTime> list = new LinkedList<ResponseTime>();
                long timeStamp = 1409814914298L;
                ResponseTime responseTime = new ResponseTime(SERVICE_NAME, ServiceType.STAND_ALONE, timeStamp);
                list.add(responseTime);
                TimeHistogram histogram = null;

                for (int i=0 ; i < 5; i++) {
                    for (int j=0 ; j < 5; j++) {
                        histogram = new TimeHistogram(ServiceType.STAND_ALONE, timeStamp);
                        histogram.addCallCountByElapsedTime(1000, false);
                        histogram.addCallCountByElapsedTime(3000, false);
                        histogram.addCallCountByElapsedTime(5000, false);
                        histogram.addCallCountByElapsedTime(6000, false);
                        histogram.addCallCountByElapsedTime(7000, false);
                        responseTime.addResponseTime("agent_" + i + "_" + j, histogram);
                    }
                    
                    timeStamp += 1;
                }
                
                return list;
            }
        };
    }
    
    @Test
    public void processTest() {
        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);
        ResponseTimeDataCollector collector = new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, mockMapResponseDAO, 3000000, System.currentTimeMillis());
        Rule rule = new Rule(SERVICE_NAME, ServiceType.STAND_ALONE.getName(), CheckerCategory.SLOW_COUNT.getName(), 74, "testGroup", false, false, "");
        AlarmChecker filter = CheckerCategory.SLOW_COUNT.createChecker(collector, rule);
        
        filter = processor.process(filter);
        assertTrue(filter.isDetected());
        
        rule = new Rule(SERVICE_NAME, ServiceType.STAND_ALONE.getName(), CheckerCategory.SLOW_COUNT.getName(), 76, "testGroup", false, false, "");
        filter = CheckerCategory.SLOW_COUNT.createChecker(collector, rule);
        
        filter = processor.process(filter);
        assertFalse(filter.isDetected());
        
    }

}
