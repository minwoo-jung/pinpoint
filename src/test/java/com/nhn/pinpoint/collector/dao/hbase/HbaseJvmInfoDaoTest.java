package com.nhn.pinpoint.collector.dao.hbase;

import java.util.Arrays;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.thrift.TSerializer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.thrift.dto.JVMInfoThriftDTO;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;

@RunWith(SpringJUnit4ClassRunner.class)
// @TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@ContextConfiguration("classpath:applicationContext-test.xml")
public class HbaseJvmInfoDaoTest {

	@Autowired
	private HbaseOperations2 hbaseOperations;

	@Autowired
	private HbaseJvmInfoDao hbaseJvmInfoDao;


	RowMapper<byte[]> valueRowMapper = new RowMapper<byte[]>() {
		@Override
		public byte[] mapRow(Result result, int rowNum) throws Exception {
			return result.value();
		}
	};

	@Test
	public void testRowKey() throws Exception {
		JVMInfoThriftDTO jvmInfoThriftDTO = new JVMInfoThriftDTO();
		jvmInfoThriftDTO.setAgentId("test");
		jvmInfoThriftDTO.setDataTime(System.currentTimeMillis());

		HbaseJvmInfoDao jvm = new HbaseJvmInfoDao();
		byte[] rowKey = jvm.getRowKey(jvmInfoThriftDTO);

		byte[] tests = Bytes.toBytes("test");
		long dataTime = jvmInfoThriftDTO.getDataTime();
		Assert.assertArrayEquals(Arrays.copyOfRange(rowKey, 0, tests.length), tests);
		Assert.assertArrayEquals(Arrays.copyOfRange(rowKey, HBaseTables.AGENT_NAME_MAX_LEN, HBaseTables.AGENT_NAME_MAX_LEN + 8), Bytes.toBytes(dataTime));
	}

    // System data관련 테이블 아직 없음.
    @Ignore
	@Test
	public void testInsert() throws Exception {

		final JVMInfoThriftDTO jvmInfoThriftDTO = new JVMInfoThriftDTO();
		jvmInfoThriftDTO.setAgentId("test_agent");
		jvmInfoThriftDTO.setDataTime(System.currentTimeMillis());

		TSerializer tSerializer = new TSerializer();
		byte[] bytes = tSerializer.serialize(jvmInfoThriftDTO);
		hbaseJvmInfoDao.insert(jvmInfoThriftDTO, bytes);

		byte[] rowKey = hbaseJvmInfoDao.getRowKey(jvmInfoThriftDTO);

		byte[] execute = hbaseOperations.get("SystemInfo", rowKey, Bytes.toBytes("JVM"), Bytes.toBytes("info"), valueRowMapper);

		Assert.assertArrayEquals(execute, bytes);
	}
}
