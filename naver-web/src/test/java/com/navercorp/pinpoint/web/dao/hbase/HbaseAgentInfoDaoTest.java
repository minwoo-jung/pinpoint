package com.navercorp.pinpoint.web.dao.hbase;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import com.navercorp.pinpoint.web.dao.hbase.HbaseAgentInfoDao;

/**
 * @author emeroad
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class HbaseAgentInfoDaoTest {

    @Autowired
    private HbaseAgentInfoDao selectDao;

    @Autowired
    private com.navercorp.pinpoint.collector.dao.hbase.HbaseAgentInfoDao insertDao;

    @Test
    public void testSelectAgentInfoStartTime() throws Exception {
        TAgentInfo agentInfo1 = createAgentInfo(10000);
        insertDao.insert(agentInfo1);

        TAgentInfo agentInfo2 = createAgentInfo(20000);
        insertDao.insert(agentInfo2);

        TAgentInfo agentInfo3 = createAgentInfo(30000);
        insertDao.insert(agentInfo3);

        AgentInfoBo testcaseAgent1 = selectDao.getAgentInfo("testcaseAgent", 20005);
        Assert.assertEquals(testcaseAgent1.getStartTime(), 20000);

        AgentInfoBo testcaseAgent2 = selectDao.getAgentInfo("testcaseAgent", 10004);
        Assert.assertEquals(testcaseAgent2.getStartTime(), 10000);

        AgentInfoBo testcaseAgent3 = selectDao.getAgentInfo("testcaseAgent", 50000);
        Assert.assertEquals(testcaseAgent3.getStartTime(), 30000);

    }

    private TAgentInfo createAgentInfo(long startTime) {
        TAgentInfo agentInfo = new TAgentInfo();
        agentInfo.setAgentId("testcaseAgent");
        agentInfo.setApplicationName("testcaseApplication");
        agentInfo.setHostname("testcaseHostName");
        agentInfo.setPorts("9995");
        agentInfo.setStartTimestamp(startTime);
        agentInfo.setServiceType(ServiceType.TEST_STAND_ALONE.getCode());

        return agentInfo;
    }
}
