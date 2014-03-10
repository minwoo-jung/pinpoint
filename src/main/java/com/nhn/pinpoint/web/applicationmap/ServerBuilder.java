package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.CallHistogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.CallHistogramList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class ServerBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CallHistogramList callHistogramList;
    private final Set<AgentInfoBo> agentSet;

    public ServerBuilder() {
        this.callHistogramList = new CallHistogramList();
        this.agentSet = new HashSet<AgentInfoBo>();
    }

    public void addCallHistogramList(CallHistogramList callHistogramList) {
        if (callHistogramList == null) {
            return;
        }
        this.callHistogramList.addCallHistogram(callHistogramList);
    }

    public void addAgentInfo(Set<AgentInfoBo> agentInfoBo) {
        if (agentInfoBo == null) {
            return;
        }
        this.agentSet.addAll(agentInfoBo);
    }

    public void addServerInstance(ServerBuilder copy) {
        if (copy == null) {
            throw new NullPointerException("copy must not be null");
        }
        addCallHistogramList(copy.callHistogramList);
        addAgentInfo(copy.agentSet);
    }



    private String getHostName(String instanceName) {
        final int pos = instanceName.indexOf(':');
        if (pos > 0) {
            return instanceName.substring(0, pos);
        } else {
            return instanceName;
        }
    }

    /**
     * 어플리케이션에 속한 물리서버와 서버 인스턴스 정보를 채운다.
     *
     * @param hostHistogram
     */
    public ServerInstanceList buildLogicalServer(final CallHistogramList hostHistogram) {
        ServerInstanceList serverInstanceList = new ServerInstanceList();
        for (CallHistogram callHistogram : hostHistogram.getCallHistogramList()) {
            final String instanceName = callHistogram.getId();
            final String hostName = getHostName(callHistogram.getId());
            final ServiceType serviceType = callHistogram.getServiceType();

            final ServerInstance serverInstance = new ServerInstance(instanceName, serviceType);
            serverInstanceList.addServerInstance(hostName, serverInstance);
        }
        return serverInstanceList;
    }

    public ServerInstanceList buildPhysicalServer(final Set<AgentInfoBo> agentSet) {
        final ServerInstanceList serverInstanceList = new ServerInstanceList();
        for (AgentInfoBo agent : agentSet) {
            final String hostName = agent.getHostname();
            final ServerInstance serverInstance = new ServerInstance(agent);
            serverInstanceList.addServerInstance(hostName, serverInstance);

        }
        return serverInstanceList;
    }



    public ServerInstanceList build() {
        if (!agentSet.isEmpty()) {
            // agent이름이 존재할 경우. 실제 리얼 서버가 존재할 경우
            this.logger.debug("buildPhysicalServer");
            return buildPhysicalServer(agentSet);
        } else {
            // 논리 이름으로 구성.
            this.logger.debug("buildLogicalServer");
            return buildLogicalServer(callHistogramList);
        }
    }


}
