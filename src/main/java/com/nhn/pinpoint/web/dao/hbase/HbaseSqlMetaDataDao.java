package com.nhn.pinpoint.web.dao.hbase;

import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.dao.SqlMetaDataDao;
import com.nhn.pinpoint.common.bo.SqlMetaDataBo;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;

/**
 *
 */
@Repository
public class HbaseSqlMetaDataDao implements SqlMetaDataDao {

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    @Qualifier("sqlMetaDataMapper")
    private RowMapper<List<SqlMetaDataBo>> sqlMetaDataMapper;

    @Override
    public List<SqlMetaDataBo> getSqlMetaData(String agentId, long time, int hashCode) {
        SqlMetaDataBo sqlMetaData = new SqlMetaDataBo(agentId, time, hashCode);
        byte[] sqlId = sqlMetaData.toRowKey();

        Get get = new Get(sqlId);
        get.addFamily(HBaseTables.SQL_METADATA_CF_SQL);

        return hbaseOperations2.get(HBaseTables.SQL_METADATA, get, sqlMetaDataMapper);
    }
}
