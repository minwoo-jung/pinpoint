package com.nhn.pinpoint.web.dao.hbase;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.applicationmap.ApplicationStatistics;
import com.nhn.pinpoint.web.dao.ApplicationMapStatisticsCallerDao;
import com.nhn.pinpoint.web.mapper.ApplicationMapLinkStatisticsMapper;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.ApplicationMapStatisticsUtils;
import com.profiler.common.util.TimeSlot;

/**
 * 
 * @author netspider
 * 
 */
@Repository
public class HbaseApplicationMapStatisticsCallerDao implements ApplicationMapStatisticsCallerDao {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private int scanCacheSize = 40;

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("applicationMapStatisticsCallerMapper")
	private RowMapper<Map<String, ApplicationStatistics>> applicationMapStatisticsCallerMapper;

	@Override
	public Map<String, ApplicationStatistics> selectCaller(String calleeApplicationName, short calleeServiceType, long from, long to) {
		Scan scan = createScan(calleeApplicationName, calleeServiceType, from, to);
		List<Map<String, ApplicationStatistics>> found = hbaseOperations2.find(HBaseTables.APPLICATION_MAP_STATISTICS_CALLER, scan, applicationMapStatisticsCallerMapper);

		Map<String, ApplicationStatistics> result = new HashMap<String, ApplicationStatistics>();

		for (Map<String, ApplicationStatistics> map : found) {
			for (Entry<String, ApplicationStatistics> entry : map.entrySet()) {
				if (result.containsKey(entry.getKey())) {
					result.get(entry.getKey()).mergeWith(entry.getValue());
				} else {
					result.put(entry.getKey(), entry.getValue());
				}
			}
		}

		return result;
	}
	
	/**
	 * 메인페이지 서버 맵에서 연결선을 선택했을 때 보여주는 통계정보.
	 * 
	 * @return <pre>
	 * list [
	 *     map {
	 *         key = timestamp
	 *         value = map {
	 *             key = histogram slot
	 *             value = count
	 *         }
	 *     }
	 * ]
	 * </pre>
	 */
	@Override
	public List<Map<Long, Map<Short, Long>>> selectCallerStatistics(String callerApplicationName, short callerServiceType, String calleeApplicationName, short calleeServiceType, long from, long to) {
		if (logger.isDebugEnabled()) {
			logger.debug("selectCallerStatistics. " + callerApplicationName + ", " + callerServiceType + ", " + calleeApplicationName + ", " + calleeServiceType + ", " + from + ", " + to);
		}
		Scan scan = createScan(calleeApplicationName, calleeServiceType, from, to);
		RowMapper<Map<Long, Map<Short, Long>>> mapper = new ApplicationMapLinkStatisticsMapper(callerApplicationName, callerServiceType, calleeApplicationName, calleeServiceType);
		return hbaseOperations2.find(HBaseTables.APPLICATION_MAP_STATISTICS_CALLER, scan, mapper);
	}

	private Scan createScan(String applicationName, short serviceType, long from, long to) {
		long startTime = TimeSlot.getStatisticsRowSlot(from);
		// hbase의 scanner를 사용하여 검색시 endTime은 검색 대상에 포함되지 않기 때문에, +1을 해줘야 된다.
		long endTime = TimeSlot.getStatisticsRowSlot(to) + 1;
		
		if (logger.isDebugEnabled()) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
			logger.debug("scan startTime:{} endTime:{}", simpleDateFormat.format(new Date(startTime)), simpleDateFormat.format(new Date(endTime)));
		}
		
		// timestamp가 reverse되었기 때문에 start, end를 바꿔서 조회.
		byte[] startKey = ApplicationMapStatisticsUtils.makeRowKey(applicationName, serviceType, endTime);
		byte[] endKey = ApplicationMapStatisticsUtils.makeRowKey(applicationName, serviceType, startTime);

		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);
		scan.setStartRow(startKey);
		scan.setStopRow(endKey);
		scan.addFamily(HBaseTables.APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER);
		scan.setId("ApplicationStatisticsScan");

		return scan;
	}
}
