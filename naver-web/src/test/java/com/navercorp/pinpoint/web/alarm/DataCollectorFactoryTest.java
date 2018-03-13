package com.navercorp.pinpoint.web.alarm;

import static org.junit.Assert.*;

import com.navercorp.pinpoint.web.namespace.RequestContextInitializer;
import com.navercorp.pinpoint.web.security.StaticOrganizationInfoAllocator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.DataCollectorFactory;
import com.navercorp.pinpoint.web.alarm.collector.DataCollector;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;

import javax.servlet.ServletRequestEvent;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class DataCollectorFactoryTest extends RequestContextInitializer {

    @Autowired
    private DataCollectorFactory factory;

    @Test
    public void createDataCollector() {
        DataCollector collector = factory.createDataCollector(CheckerCategory.SLOW_COUNT, null, 0);
        assertNotNull(collector);
    }

}
