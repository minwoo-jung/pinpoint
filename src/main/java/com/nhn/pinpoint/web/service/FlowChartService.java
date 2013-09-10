package com.nhn.pinpoint.web.service;

import java.util.List;
import java.util.Set;

import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.calltree.server.ServerCallTree;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.BusinessTransactions;
import com.nhn.pinpoint.web.vo.LinkStatistics;
import com.nhn.pinpoint.web.vo.TransactionId;

/**
 * @author netspider
 */
public interface FlowChartService {

	public Set<TransactionId> selectTraceIdsFromApplicationTraceIndex(String applicationName, long from, long to);

	public List<Application> selectAllApplicationNames();

	public ServerCallTree selectServerCallTree(TransactionId traceId);

	public BusinessTransactions selectBusinessTransactions(Set<TransactionId> traceIds, String applicationName, long from, long to, Filter filter);
	
	@Deprecated
	public ServerCallTree selectServerCallTree(Set<TransactionId> traceIdSet, Filter filter);
	
	public LinkStatistics linkStatistics(long from, long to, String srcApplicationName, short srcServiceType, String destApplicationName, short destServiceType);

	public LinkStatistics linkStatisticsDetail(long from, long to, Set<TransactionId> traceIdSet, String srcApplicationName, short srcServiceType, String destApplicationName, short destServiceType, Filter filter);

	public ApplicationMap selectApplicationMap(Set<TransactionId> traceIdSet, long from, long to, Filter filter);
}
