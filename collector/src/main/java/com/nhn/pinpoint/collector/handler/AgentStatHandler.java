package com.nhn.pinpoint.collector.handler;

import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TAgentStatBatch;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.collector.dao.AgentStatDao;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
@Service("agentStatHandler")
public class AgentStatHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(AgentStatHandler.class.getName());

    @Autowired
    private AgentStatDao agentStatDao;

    public void handle(TBase<?, ?> tbase, byte[] packet, int offset, int length) {
        // FIXME (2014.08) Legacy - TAgentStats should not be sent over the wire.
        if (tbase instanceof TAgentStat) {
            final TAgentStat agentStat = (TAgentStat)tbase;
            String agentId = agentStat.getAgentId();
            long startTimestamp = agentStat.getStartTimestamp();
            handleAgentStat(agentId, startTimestamp, agentStat);
        } else if (tbase instanceof TAgentStatBatch) {
            handleAgentStatBatch((TAgentStatBatch)tbase);
        } else {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + TAgentStat.class.getName() + " or " + TAgentStatBatch.class.getName());
        }
    }

    private <T extends TAgentStat> void handleAgentStat(String agentId, long startTimestamp, T agentStat) {
        try {
            agentStat.setAgentId(agentId);
            agentStat.setStartTimestamp(startTimestamp);
            agentStatDao.insert(agentStat);
        } catch (Exception e) {
            logger.warn("AgentStat handle error. Caused:{}", e.getMessage());
        }
    }

    private <T extends TAgentStatBatch> void handleAgentStatBatch(T agentStatBatch) {
        if (logger.isDebugEnabled()) {
            logger.debug("Received AgentStats={}", agentStatBatch);
        }
        String agentId = agentStatBatch.getAgentId();
        long startTimestamp = agentStatBatch.getStartTimestamp();
        for (TAgentStat agentStat : agentStatBatch.getAgentStats()) {
            handleAgentStat(agentId, startTimestamp, agentStat);
        }
    }
}
