package com.nhn.pinpoint.web.applicationmap;

import java.io.IOException;
import java.util.*;

import com.nhn.pinpoint.web.applicationmap.rawdata.CallHistogramList;
import com.nhn.pinpoint.web.view.NodeSerializer;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.ResponseHistogramSummary;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;

/**
 * node map에서 application을 나타낸다.
 * 
 * @author netspider
 * @author emeroad
 */
@JsonSerialize(using = NodeSerializer.class)
public class Node {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String NODE_DELIMITER = "^";
    private final Application application;

    private ServerBuilder serverBuilder = new ServerBuilder();
    private ServerInstanceList serverInstanceList;


    private ResponseHistogramSummary responseHistogramSummary;
    // 임시로 생성.
    private static final ObjectMapper MAPPER = new ObjectMapper();


	public Node(Application application, Set<AgentInfoBo> agentSet) {
        this(application, null, agentSet);
	}

    public Node(Application application, CallHistogramList callHistogramList) {
        this(application, callHistogramList, null);
    }

    Node(Application application, CallHistogramList callHistogramList, Set<AgentInfoBo> agentSet) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }

        logger.debug("create node application={}, agentSet={}", application, agentSet);
        this.application = application;

        this.serverBuilder.addCallHistogramList(callHistogramList);
        this.serverBuilder.addAgentInfo(agentSet);

    }

    public Node(Node copyNode) {
        if (copyNode == null) {
            throw new NullPointerException("copyNode must not be null");
        }
        this.application = copyNode.application;
        this.serverBuilder.addServerInstance(copyNode.serverBuilder);

    }

    public String getApplicationTextName() {
        if (application.getServiceType().isUser()) {
            return "USER";
        } else {
            return application.getName();
        }
    }

    void build() {
        this.serverInstanceList = serverBuilder.build();
        this.serverBuilder = null;
    }
	
	public ServerInstanceList getServerInstanceList() {
		return serverInstanceList;
	}

    @JsonIgnore
    public String getNodeJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public String getJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        sb.append("\"applicationName\" : \"").append(getApplicationTextName()).append("\",");
        sb.append("\"serviceType\" : \"").append(application.getServiceType()).append("\",");
        sb.append("\"serviceTypeCode\" : \"").append(application.getServiceTypeCode()).append("\"");
        sb.append(" }");
        return sb.toString();
    }



    public Application getApplication() {
        return application;
    }



	public String getNodeName() {
		return application.getName() + NODE_DELIMITER + application.getServiceType();
	}


	public void add(Node node) {
        if (node == null) {
            throw new NullPointerException("node must not be null");
        }
        logger.trace("merge node this={}, node={}", this.application, node.application);
		
        // 리얼 application을 실제빌드할때 copy하여 만들기 때문에. add할때 데이터를 hostList를 add해도 된다.

        this.serverBuilder.addServerInstance(node.serverBuilder);

	}

	public ServiceType getServiceType() {
		return application.getServiceType();
	}

    public ResponseHistogramSummary getResponseHistogramSummary() {
        return responseHistogramSummary;
    }

    public void setResponseHistogramSummary(ResponseHistogramSummary responseHistogramSummary) {
        this.responseHistogramSummary = responseHistogramSummary;
    }

	@Override
	public String toString() {
		return "Node [application=" + application + ", serverInstanceList=" + serverInstanceList + "]";
	}
}
