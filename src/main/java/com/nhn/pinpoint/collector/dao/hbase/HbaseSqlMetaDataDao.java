package com.nhn.pinpoint.collector.dao.hbase;

import com.nhn.pinpoint.common.bo.SqlMetaDataBo;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.thrift.dto.SqlMetaData;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.collector.dao.SqlMetaDataDao;
import org.springframework.stereotype.Repository;

/**
 *
 */
@Repository
public class HbaseSqlMetaDataDao implements SqlMetaDataDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Override
    public void insert(SqlMetaData sqlMetaData) {
        if (logger.isDebugEnabled()) {
            logger.debug("insert:" + sqlMetaData);
        }

        SqlMetaDataBo sqlMetaDataBo = new SqlMetaDataBo(sqlMetaData.getAgentId(), sqlMetaData.getHashCode(), sqlMetaData.getAgentStartTime());
        byte[] rowKey = sqlMetaDataBo.toRowKey();


        Put put = new Put(rowKey);
        String sql = sqlMetaData.getSql();
        byte[] sqlBytes = Bytes.toBytes(sql);
        // hashCode가 충돌날수 있으므로 일부러 qualifier에 넣음.
        put.add(HBaseTables.SQL_METADATA_CF_SQL, sqlBytes, null);

        hbaseTemplate.put(HBaseTables.SQL_METADATA, put);
    }
}
