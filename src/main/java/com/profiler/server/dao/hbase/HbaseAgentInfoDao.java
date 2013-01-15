package com.profiler.server.dao.hbase;

import com.profiler.common.dto.thrift.AgentInfo;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.RowKeyUtils;
import com.profiler.common.util.TimeUtils;
import com.profiler.server.dao.AgentInfoDao;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class HbaseAgentInfoDao implements AgentInfoDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Override
    public void insert(AgentInfo agentInfo) {

        byte[] agentId = Bytes.toBytes(agentInfo.getAgentId());
        long reverseKey = TimeUtils.reverseCurrentTimeMillis(agentInfo.getTimestamp());
        byte[] rowKey = RowKeyUtils.concatFixedByteAndLong(agentId, RowKeyUtils.AGENT_NAME_LIMIT, reverseKey);
        Put put = new Put(rowKey);

//          추가 agent 정보를 넣어야 됨. 일단 sqlMetaData에 필요한 starttime만 넣음.
        put.add(HBaseTables.AGENTINFO_CF_INFO, null, null);

        hbaseTemplate.put(HBaseTables.AGENTINFO, put);

    }
}
