package com.nhn.pinpoint.web.mapper;

import java.util.*;

import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.LinkKey;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;

/**
 * 
 * @author netspider
 * 
 */
@Component
public class MapStatisticsCallerMapper implements RowMapper<List<LinkStatistics>> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<LinkStatistics> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        logger.debug("mapRow:{}", rowNum);
		final KeyValue[] keyList = result.raw();

        final Map<LinkKey, LinkStatistics> linkStatisticsMap = new HashMap<LinkKey, LinkStatistics>();

		for (KeyValue kv : keyList) {

            final byte[] row = kv.getRow();
            Application calleeApplication = readCalleeApplication(row);

            final byte[] qualifier = kv.getQualifier();
            Application callerApplication = readCallerApplication(qualifier);

            long requestCount = Bytes.toLong(kv.getValue());
            short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);

            String callerHost = ApplicationMapStatisticsUtils.getHost(qualifier);
            boolean isError = histogramSlot == (short) -1;

            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched Caller. {} callerHost:{} -> {} (slot:{}/{}),  ", callerApplication, callerHost, calleeApplication, histogramSlot, requestCount);
            }

            LinkStatistics statistics = getLinkStatics(linkStatisticsMap, callerApplication, calleeApplication);
            statistics.addSample(callerHost, calleeApplication.getServiceTypeCode(), (isError) ? (short) -1 : histogramSlot, requestCount);

            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched Caller. statistics:{}", statistics);
            }
		}

        return new ArrayList<LinkStatistics>(linkStatisticsMap.values());
	}

    private LinkStatistics getLinkStatics(Map<LinkKey, LinkStatistics> linkStatisticsMap, Application callerApplication, Application calleeApplication) {
        final LinkKey key = new LinkKey(callerApplication, calleeApplication);
        LinkStatistics statistics = linkStatisticsMap.get(key);
        if (statistics == null) {
            statistics = new LinkStatistics(callerApplication, calleeApplication);
            linkStatisticsMap.put(key, statistics);
        }
        return statistics;
    }

    private Application readCallerApplication(byte[] qualifier) {
        String callerApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
        short callerServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);
        return new Application(callerApplicationName, callerServiceType);
    }

    private Application readCalleeApplication(byte[] row) {
        String calleeApplicationName = ApplicationMapStatisticsUtils.getApplicationNameFromRowKey(row);
        short calleeServiceType = ApplicationMapStatisticsUtils.getApplicationTypeFromRowKey(row);
        return new Application(calleeApplicationName, calleeServiceType);
    }
}
