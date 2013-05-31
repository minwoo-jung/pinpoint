package com.nhn.pinpoint.web.dao.hbase;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.RowKeyUtils;
import com.nhn.pinpoint.common.util.TimeUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.dao.AgentInfoDao;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;

/**
 *
 */
@Repository
public class HbaseAgentInfoDao implements AgentInfoDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    /**
     * currentTime에서 가장 근접한 시간의 agent startTime을 find한다.
     *
     * @param agentId
     * @param currentTime
     * @return
     */
    @Override
    public AgentInfoBo findAgentInfoBeforeStartTime(final String agentId, final long currentTime) {
        // TODO cache를 걸어야 될듯 하다.
        Scan scan = createScan(agentId, currentTime);
        AgentInfoBo agentInfoBo = hbaseOperations2.find(HBaseTables.AGENTINFO, scan, new ResultsExtractor<AgentInfoBo>() {
            @Override
            public AgentInfoBo extractData(ResultScanner results) throws Exception {
                for (Result next; (next = results.next()) != null; ) {
                    byte[] row = next.getRow();
                    long reverseStartTime = BytesUtils.bytesToLong(row, HBaseTables.AGENT_NAME_MAX_LEN);
                    long startTime = TimeUtils.recoveryCurrentTimeMillis(reverseStartTime);
                    logger.debug("agent:{} startTime value {}", agentId, startTime);
                    // 바로 전 시작 시간을 찾아야 한다.
                    if (startTime < currentTime) {
                        byte[] value = next.getValue(HBaseTables.AGENTINFO_CF_INFO, HBaseTables.AGENTINFO_CF_INFO_IDENTIFIER);
                        AgentInfoBo agentInfoBo = new AgentInfoBo();
                        agentInfoBo.setAgentId(agentId);
                        agentInfoBo.setTimestamp(startTime);
                        agentInfoBo.readValue(value);
                        
                        logger.debug("agent:{} startTime find {}", agentId, startTime);

                        return agentInfoBo;
                    }
                }
                
                logger.warn("agentInfo not found. agentId={}, time={}", agentId, currentTime);
                
                return null;
            }
        });

//        if (startTime == null) {
//            return -1;
//        }
        return agentInfoBo;
    }

    private Scan createScan(String agentInfo, long currentTime) {
        Scan scan = new Scan();
        scan.setCaching(20);

        byte[] agentIdBytes = Bytes.toBytes(agentInfo);
        long startTime = TimeUtils.reverseCurrentTimeMillis(currentTime);
        byte[] startKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, startTime);
        scan.setStartRow(startKeyBytes);

        byte[] endKeyBytes = RowKeyUtils.concatFixedByteAndLong(agentIdBytes, HBaseTables.AGENT_NAME_MAX_LEN, Long.MAX_VALUE);
        scan.setStopRow(endKeyBytes);
        scan.addFamily(HBaseTables.AGENTINFO_CF_INFO);

        return scan;
    }
}
