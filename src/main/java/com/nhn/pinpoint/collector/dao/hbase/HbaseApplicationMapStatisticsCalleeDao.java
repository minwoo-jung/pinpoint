package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER;

import org.apache.hadoop.hbase.client.Increment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.dao.ApplicationMapStatisticsCalleeDao;
import com.nhn.pinpoint.collector.dao.hbase.StatisticsCache.FlushHandler;
import com.nhn.pinpoint.collector.dao.hbase.StatisticsCache.Value;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import org.springframework.stereotype.Repository;

/**
 * 나를 호출한 application 통계 갱신
 * 
 * @author netspider
 */
@Repository
public class HbaseApplicationMapStatisticsCalleeDao implements ApplicationMapStatisticsCalleeDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Autowired
	private AcceptedTimeService acceptedTimeService;

	private final boolean useBulk;
	private final StatisticsCache cache;

	public HbaseApplicationMapStatisticsCalleeDao() {
		this.useBulk = true;
		this.cache = createCache();
	}

	public HbaseApplicationMapStatisticsCalleeDao(boolean useBulk) {
		this.useBulk = useBulk;
		this.cache = (useBulk) ? createCache() : null;
	}

    private StatisticsCache createCache() {
        return new StatisticsCache(new FlushHandler() {
            @Override
            public void handleValue(Value value) {
                hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLEE, value.getRowKey(), APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER, value.getColumnName(), value.getLongValue());
            }

            @Override
            public void handleValue(Increment increment) {
                hbaseTemplate.increment(APPLICATION_MAP_STATISTICS_CALLEE, increment);
            }
        });
    }

    @Override
	public void update(String callerApplicationName, short callerServiceType, String calleeApplicationName, short calleeServiceType, String calleeHost, int elapsed, boolean isError) {
		if (calleeApplicationName == null) {
			throw new IllegalArgumentException("calleeApplicationName is null.");
		}

		if (callerApplicationName == null) {
			throw new IllegalArgumentException("callerApplicationName is null.");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("[UpdatingApplicationMapStatisticsCallee] " + callerApplicationName + " (" + ServiceType.findServiceType(callerServiceType) + ")[" + calleeHost + "] -> " + calleeApplicationName + " (" + ServiceType.findServiceType(calleeServiceType) + ")");
		}

		// make row key. rowkey는 나.
		long acceptedTime = acceptedTimeService.getAcceptedTime();
		long rowTimeSlot = TimeSlot.getStatisticsRowSlot(acceptedTime);
		final byte[] rowKey = ApplicationMapStatisticsUtils.makeRowKey(calleeApplicationName, calleeServiceType, rowTimeSlot);

		// column name은 나를 호출한 app
		byte[] columnName = ApplicationMapStatisticsUtils.makeColumnName(callerServiceType, callerApplicationName, calleeHost, elapsed, isError);

		if (useBulk) {
			cache.add(rowKey, columnName, 1L);
		} else {
			hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLEE, rowKey, APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER, columnName, 1L);
		}
	}

	@Override
	public void flushAll() {
		if (!useBulk) {
			throw new IllegalStateException();
		}
		cache.flushAll();
	}
}
