package com.nhn.hippo.web.dao.hbase;

import java.util.List;

import org.apache.hadoop.hbase.client.Scan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.hippo.web.dao.TerminalStatisticsDao;
import com.nhn.hippo.web.vo.TerminalRequest;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.TerminalSpanUtils;
import com.profiler.common.util.TimeSlot;

/**
 * 
 * @author netspider
 * 
 */
@Repository
public class HbaseTerminalStatisticsDao implements TerminalStatisticsDao {

	private int scanCacheSize = 40;

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("terminalRequestCountMapper")
	private RowMapper<List<TerminalRequest>> terminalRequestCountMapper;

	@Override
	public List<List<TerminalRequest>> selectTerminal(String applicationName, long from, long to) {
		Scan scan = createScan(applicationName, from, to);
		return hbaseOperations2.find(HBaseTables.TERMINAL_STATISTICS, scan, terminalRequestCountMapper);
	}

	private Scan createScan(String applicationName, long from, long to) {
		byte[] startKey = TerminalSpanUtils.makeRowKey(applicationName, TimeSlot.getSlot(from));
		byte[] endKey = TerminalSpanUtils.makeRowKey(applicationName, TimeSlot.getSlot(to));

		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);
		scan.setStartRow(startKey);
		scan.setStopRow(endKey);
		scan.addFamily(HBaseTables.TERMINAL_STATISTICS_CF_COUNTER);
		scan.setId("terminalStatisticsScan");

		return scan;
	}
}
