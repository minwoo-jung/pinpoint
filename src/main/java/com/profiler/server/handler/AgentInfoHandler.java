package com.profiler.server.handler;

import java.net.DatagramPacket;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.dto.thrift.AgentInfo;
import com.profiler.common.dto.thrift.Span;
import com.profiler.server.dao.ApplicationIndex;

public class AgentInfoHandler implements Handler {

	private final Logger logger = LoggerFactory.getLogger(AgentInfoHandler.class.getName());

	@Autowired
	private ApplicationIndex applicationIndexDao;

	public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
		assert (tbase instanceof Span);

		try {
			AgentInfo agentInfo = (AgentInfo) tbase;

			logger.debug("Received AgentInfo=%s", agentInfo);

			applicationIndexDao.insert(agentInfo);
		} catch (Exception e) {
			logger.warn("Span handle error " + e.getMessage(), e);
		}
	}
}
