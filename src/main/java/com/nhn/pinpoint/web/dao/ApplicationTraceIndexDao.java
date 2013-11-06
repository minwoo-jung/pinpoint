package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.web.vo.LimitedScanResult;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.scatter.Dot;

/**
 * @author emeroad
 */
public interface ApplicationTraceIndexDao {
	LimitedScanResult<List<TransactionId>> scanTraceIndex(String applicationName, long start, long end, int limit);

	List<Dot> scanTraceScatter(String applicationName, long start, long end, int limit);
}
