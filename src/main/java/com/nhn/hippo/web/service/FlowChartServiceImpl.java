package com.nhn.hippo.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.nhn.hippo.web.calltree.server.AgentIdNodeSelector;
import com.nhn.hippo.web.calltree.server.ApplicationIdNodeSelector;
import com.profiler.common.bo.SpanEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.dao.ApplicationIndexDao;
import com.nhn.hippo.web.dao.ApplicationTraceIndexDao;
import com.nhn.hippo.web.dao.TerminalStatisticsDao;
import com.nhn.hippo.web.dao.TraceDao;
import com.nhn.hippo.web.dao.TraceIndexDao;
import com.nhn.hippo.web.vo.BusinessTransactions;
import com.nhn.hippo.web.vo.TerminalStatistics;
import com.nhn.hippo.web.vo.TraceId;
import com.nhn.hippo.web.vo.scatter.Dot;
import com.profiler.common.ServiceType;
import com.profiler.common.bo.SpanBo;

/**
 * @author netspider
 */
@Service
public class FlowChartServiceImpl implements FlowChartService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TraceDao traceDao;

	@Autowired
	private TraceIndexDao traceIndexDao;

	@Autowired
	private ApplicationIndexDao applicationIndexDao;

	@Autowired
	private ApplicationTraceIndexDao applicationTraceIndexDao;

	@Autowired
	private TerminalStatisticsDao terminalStatisticsDao;

	@Override
	public List<String> selectAllApplicationNames() {
		return applicationIndexDao.selectAllApplicationNames();
	}

	@Override
	public String[] selectAgentIdsFromApplicationName(String applicationName) {
		return applicationIndexDao.selectAgentIds(applicationName);
	}

	@Override
	public Set<TraceId> selectTraceIdsFromTraceIndex(String[] agentIds, long from, long to) {
		if (agentIds == null) {
			throw new NullPointerException("agentIds");
		}

		if (agentIds.length == 1) {
			// single scan
			if (logger.isTraceEnabled()) {
				logger.trace("scan {}, {}, {}", new Object[] { agentIds[0], from, to });
			}
			List<List<byte[]>> bytes = this.traceIndexDao.scanTraceIndex(agentIds[0], from, to);
			Set<TraceId> result = new HashSet<TraceId>();
			for (List<byte[]> list : bytes) {
				for (byte[] traceId : list) {
					TraceId tid = new TraceId(traceId);
					result.add(tid);
					logger.trace("traceid:{}", tid);
				}
			}
			return result;
		} else {
			// multi scan 가능한 동일 open htable 에서 액세스함.
			List<List<List<byte[]>>> multiScan = this.traceIndexDao.multiScanTraceIndex(agentIds, from, to);
			Set<TraceId> result = new HashSet<TraceId>();
			for (List<List<byte[]>> list : multiScan) {
				for (List<byte[]> scan : list) {
					for (byte[] traceId : scan) {
						result.add(new TraceId(traceId));
					}
				}
			}
			return result;
		}
	}

	@Deprecated
	@Override
	public ServerCallTree selectServerCallTree(Set<TraceId> traceIds) {
		final ServerCallTree tree = new ServerCallTree(new ApplicationIdNodeSelector());

		List<List<SpanBo>> traces = this.traceDao.selectSpans(traceIds);

		for (List<SpanBo> transaction : traces) {
			// List<SpanBo> processed = refine(transaction);
			// markRecursiveCall(transaction);
			for (SpanBo eachTransaction : transaction) {
				tree.addSpan(eachTransaction);
			}
		}
		return tree.build();
	}

	/**
     * DetailView에서 사용함. 하나의 Span을 선택했을때 Draw되는 데이터를 생성하는 함수이다
	 * makes call tree of transaction detail view
	 */
	@Override
	public ServerCallTree selectServerCallTree(TraceId traceId) {


		List<SpanBo> transaction = this.traceDao.selectSpans(traceId);

		Set<String> endPoints = createUniqueEndpoint(transaction);
        ServerCallTree tree = createServerCallTree(transaction);

        // subSpan에서 record할 데이터만 골라낸다.
        List<SpanEvent> spanEventBoList = findRecordStatisticsSpanEventData(transaction, endPoints);
        tree.addSpanEventList(spanEventBoList);

        return tree.build();
	}

    private List<SpanEvent> findRecordStatisticsSpanEventData(List<SpanBo> transaction, Set<String> endPoints) {
        List<SpanEvent> filterSpanEvent = new ArrayList<SpanEvent>();
        for (SpanBo eachTransaction : transaction) {
			List<SpanEvent> spanEventBoList = eachTransaction.getSpanEventBoList();

			if (spanEventBoList == null) {
				continue;
            }

			for (SpanEvent spanEventBo : spanEventBoList) {
                // 통계정보로 잡지 않을 데이터는 스킵한다.
                if (!spanEventBo.getServiceType().isRecordStatistics()) {
                    continue;
                }

				// remove subspan of the rpc client
				if (!endPoints.contains(spanEventBo.getEndPoint())) {
					// this is unknown cloud
                    filterSpanEvent.add(spanEventBo);
				}
			}
		}
        return filterSpanEvent;
    }

    /**
     * ServerCallTree를 생성하고 low SpanBo데이터를 tree에 추가한다.
     * @param transaction
     * @return
     */
    private ServerCallTree createServerCallTree(List<SpanBo> transaction) {
        ServerCallTree serverCallTree = new ServerCallTree(new AgentIdNodeSelector());
        serverCallTree.addSpanList(transaction);
        return serverCallTree;
    }

    /**
     * Trace uuid를 구성하는 SpanBo의 집합에서 endpoint 값을 유니크한 값으로 뽑아온다.
     * @param transaction
     * @return
     */
    private Set<String> createUniqueEndpoint(List<SpanBo> transaction) {
        // markRecursiveCall(transaction);
        Set<String> endPointSet = new HashSet<String>();
        for (SpanBo eachTransaction : transaction) {
            endPointSet.add(eachTransaction.getEndPoint());
        }
        return endPointSet;
    }

    /**
	 * 메인화면에서 사용. 시간별로 TimeSlot을 조회하여 서버 맵을 그릴 때 사용한다.
     * makes call tree of main view
	 */
	@Override
	public ServerCallTree selectServerCallTree(Set<TraceId> traceIds, String applicationName, long from, long to) {
		final Map<String, ServiceType> terminalQueryParams = new HashMap<String, ServiceType>();
		final ServerCallTree tree = new ServerCallTree(new ApplicationIdNodeSelector());

		StopWatch watch = new StopWatch();
		watch.start("scanNonTerminalSpans");

		// fetch non-terminal spans
		List<List<SpanBo>> traces = this.traceDao.selectSpans(traceIds);

		watch.stop();
		int totalNonTerminalSpansCount = 0;

		Set<String> endPoints = new HashSet<String>();

		// processing spans
		for (List<SpanBo> transaction : traces) {
			totalNonTerminalSpansCount += transaction.size();

			// List<SpanBo> processed = refine(transaction);
			// markRecursiveCall(transaction);
			for (SpanBo eachTransaction : transaction) {
				tree.addSpan(eachTransaction);

				// make query param
				terminalQueryParams.put(eachTransaction.getServiceName(), eachTransaction.getServiceType());

				endPoints.add(eachTransaction.getEndPoint());
			}
		}

		if (logger.isInfoEnabled()) {
			logger.info("Fetch non-terminal spans elapsed : {}ms, {} traces, {} spans", new Object[] { watch.getLastTaskTimeMillis(), traces.size(), totalNonTerminalSpansCount });
		}

		watch.start("scanTerminalStatistics");

		// fetch terminal info
		for (Entry<String, ServiceType> param : terminalQueryParams.entrySet()) {
			ServiceType svcType = param.getValue();
			if (!svcType.isRpcClient() && !svcType.isUnknown() && !svcType.isTerminal()) {
				long start = System.currentTimeMillis();
				List<Map<String, TerminalStatistics>> terminals = terminalStatisticsDao.selectTerminal(param.getKey(), from, to);
				logger.info("	Fetch terminals of {} : {}ms", param.getKey(), System.currentTimeMillis() - start);

				for (Map<String, TerminalStatistics> terminal : terminals) {
					for (Entry<String, TerminalStatistics> entry : terminal.entrySet()) {
						// TODO 임시방편
						TerminalStatistics terminalStatistics = entry.getValue();

						// 이 요청의 destination이 수집된 trace정보에 없으면 unknown cloud로 처리한다.
						if (!endPoints.contains(terminalStatistics.getTo())) {

							if (ServiceType.findServiceType(terminalStatistics.getToServiceType()).isRpcClient()) {
								terminalStatistics.setToServiceType(ServiceType.UNKNOWN_CLOUD.getCode());
							}

							tree.addTerminalStatistics(terminalStatistics);
						}
					}
				}
			}
		}

		watch.stop();
		logger.info("Fetch terminal statistics elapsed : {}ms", watch.getLastTaskTimeMillis());

		return tree.build();
	}

	@Deprecated
	private SpanBo findChildSpan(final List<SpanBo> list, final SpanBo parent) {
		for (int i = 0; i < list.size(); i++) {
			SpanBo child = list.get(i);

			if (child.getParentSpanId() == parent.getSpanId()) {
				return child;
			}
		}
		return null;
	}

	@Deprecated
	private List<SpanBo> refine(final List<SpanBo> list) {
		for (int i = 0; i < list.size(); i++) {
			SpanBo span = list.get(i);

			if (span.getServiceType().isRpcClient()) {
				SpanBo child = findChildSpan(list, span);

				if (child != null) {
					child.setParentSpanId(span.getParentSpanId());
					child.getAnnotationBoList().addAll(span.getAnnotationBoList());
					list.remove(i);
					i--;
					continue;
				} else {
					// using as a terminal node.
					span.setServiceName(span.getEndPoint());
					span.setServiceType(ServiceType.UNKNOWN_CLOUD);
				}
			}
		}
		return list;
	}

	/**
	 * server map이 recursive call을 표현할 수 있게 되어 필요 없음.
	 * @param list
	 */
	@Deprecated
	private void markRecursiveCall(final List<SpanBo> list) {
		/*
		for (int i = 0; i < list.size(); i++) {
			SpanBo a = list.get(i);
			for (int j = 0; j < list.size(); j++) {
				if (i == j)
					continue;
				SpanBo b = list.get(j);
				if (a.getServiceName().equals(b.getServiceName()) && a.getSpanId() == b.getParentSpanId()) {
					a.increaseRecursiveCallCount();
				}
			}
		}
		*/
	}

	@Override
	public Set<TraceId> selectTraceIdsFromApplicationTraceIndex(String applicationName, long from, long to) {
		if (applicationName == null) {
			throw new NullPointerException("applicationName");
		}

		if (logger.isTraceEnabled()) {
			logger.trace("scan {}, {}, {}", new Object[] { applicationName, from, to });
		}

		List<List<byte[]>> bytes = this.applicationTraceIndexDao.scanTraceIndex(applicationName, from, to);
		Set<TraceId> result = new HashSet<TraceId>();
		for (List<byte[]> list : bytes) {
			for (byte[] traceId : list) {
				TraceId tid = new TraceId(traceId);
				result.add(tid);
				logger.trace("traceid:{}", tid);
			}
		}
		return result;
	}

	@Deprecated
	@Override
	public String[] selectAgentIds(String[] hosts) {
//		List<HbaseColumn> column = new ArrayList<HBaseQuery.HbaseColumn>();
//		column.add(new HbaseColumn("Agents", "AgentID"));
//
//		HBaseQuery query = new HBaseQuery(HBaseTables.APPLICATION_INDEX, null, null, column);
//		Iterator<Map<String, byte[]>> iterator = client.getHBaseData(query);
//
//		if (logger.isDebugEnabled()) {
//			while (iterator.hasNext()) {
//				logger.debug("selectedAgentId={}", iterator.next());
//			}
//			logger.debug("!!!==============WARNING==============!!!");
//			logger.debug("!!! selectAgentIds IS NOT IMPLEMENTED !!!");
//			logger.debug("!!!===================================!!!");
//		}

		return hosts;
	}

	@Override
	public List<Dot> selectScatterData(String applicationName, long from, long to) {
		List<List<Dot>> scanTrace = applicationTraceIndexDao.scanTraceScatter(applicationName, from, to);

		List<Dot> list = new ArrayList<Dot>();

		for (List<Dot> l : scanTrace) {
			for (Dot dot : l) {
				list.add(dot);
			}
		}

		return list;
	}
	
	@Override
	public List<Dot> selectScatterData(String applicationName, long from, long to, int limit) {
		return applicationTraceIndexDao.scanTraceScatter2(applicationName, from, to, limit);
	}

	@Override
	public BusinessTransactions selectBusinessTransactions(Set<TraceId> traceIds, String applicationName, long from, long to) {
		List<List<SpanBo>> traces = this.traceDao.selectSpans(traceIds);

		BusinessTransactions businessTransactions = new BusinessTransactions();

		for (List<SpanBo> transaction : traces) {
			for (SpanBo eachTransaction : transaction) {
				businessTransactions.add(eachTransaction);
			}
		}

		return businessTransactions;
	}
}
