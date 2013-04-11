package com.profiler.server.dao.hbase;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.bo.AgentInfoBo;
import com.profiler.common.dto2.thrift.AgentInfo;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.RowKeyUtils;
import com.profiler.common.util.TimeUtils;
import com.profiler.server.dao.AgentInfoDao;

/**
 *
 */
public class HbaseAgentInfoDao implements AgentInfoDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Override
	public void insert(AgentInfo agentInfo) {
		if (logger.isDebugEnabled()) {
			logger.debug("insert agent info. {}", agentInfo);
		}

		byte[] agentId = Bytes.toBytes(agentInfo.getAgentId());
		long reverseKey = TimeUtils.reverseCurrentTimeMillis(agentInfo.getTimestamp());
		byte[] rowKey = RowKeyUtils.concatFixedByteAndLong(agentId, HBaseTables.AGENT_NAME_MAX_LEN, reverseKey);
		Put put = new Put(rowKey);

		// 추가 agent 정보를 넣어야 됨. 일단 sqlMetaData에 필요한 starttime만 넣음.
		AgentInfoBo agentInfoBo = new AgentInfoBo(agentInfo);
		byte[] bytes = agentInfoBo.writeValue();

		put.add(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_IDENTIFIER, bytes);

		hbaseTemplate.put(HBaseTables.AGENTINFO, put);
	}
}
