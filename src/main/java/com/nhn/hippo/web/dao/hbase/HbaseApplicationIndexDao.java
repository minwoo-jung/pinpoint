package com.nhn.hippo.web.dao.hbase;

import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.hippo.web.dao.ApplicationIndexDao;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;

/**
 * @author netspider
 */
@Repository
public class HbaseApplicationIndexDao implements ApplicationIndexDao {

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("applicationNameMapper")
	private RowMapper<String> applicationNameMapper;

	@Autowired
	@Qualifier("agentIdMapper")
	private RowMapper<String[]> agentIdMapper;

	@Override
	public List<String> selectAllApplicationNames() {
		Scan scan = new Scan();
		scan.setCaching(30);
		return hbaseOperations2.find(HBaseTables.APPLICATION_INDEX, scan, applicationNameMapper);
	}

	@Override
	public String[] selectAgentIds(String applicationName) {
		byte[] rowKey = Bytes.toBytes(applicationName);

		Get get = new Get(rowKey);
		get.addFamily(HBaseTables.APPLICATION_INDEX_CF_AGENTS);

		return hbaseOperations2.get(HBaseTables.APPLICATION_INDEX, get, agentIdMapper);
	}
}
