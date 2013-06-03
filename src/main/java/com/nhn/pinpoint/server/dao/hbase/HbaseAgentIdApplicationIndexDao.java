package com.nhn.pinpoint.server.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENTID_APPLICATION_INDEX;

import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENTID_APPLICATION_INDEX_CF_APPLICATION;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;

import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.server.dao.AgentIdApplicationIndexDao;

/**
 * find applicationname by agentId
 * 
 * @author netspider
 * 
 */
public class HbaseAgentIdApplicationIndexDao implements AgentIdApplicationIndexDao {

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Autowired
	@Qualifier("applicationNameMapper")
	private RowMapper<String> applicationNameMapper;

	@Override
	public void insert(String agentId, String applicationName) {
		byte[] agentIdByte = Bytes.toBytes(agentId);
		byte[] appNameByte = Bytes.toBytes(applicationName);

		Put put = new Put(agentIdByte);
		put.add(AGENTID_APPLICATION_INDEX_CF_APPLICATION, appNameByte, appNameByte);

		hbaseTemplate.put(AGENTID_APPLICATION_INDEX, put);
	}

	@Override
	public String selectApplicationName(String agentId) {
		byte[] rowKey = Bytes.toBytes(agentId);
		Get get = new Get(rowKey);
		get.addFamily(AGENTID_APPLICATION_INDEX_CF_APPLICATION);

		return hbaseTemplate.get(AGENTID_APPLICATION_INDEX, get, applicationNameMapper);
	}
}
