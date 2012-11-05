package com.profiler.server.dao;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.dto.thrift.AgentInfo;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;

public class HbaseApplicationsIndexDao implements ApplicationsIndex {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Override
	public void insert(final AgentInfo agentInfo) {
		Put put = new Put(Bytes.toBytes(agentInfo.getApplicationName()), agentInfo.getTimestamp());
		byte[] agentId = Bytes.toBytes(agentInfo.getAgentId());
		put.add(HBaseTables.APPLICATIONS_CF_AGENTS, agentId, agentId);

		hbaseTemplate.put(HBaseTables.APPLICATIONS_INDEX, put);

		if (logger.isDebugEnabled()) {
			logger.debug("Insert agentInfo. %s", agentInfo);
		}
	}
}
