package com.nhn.pinpoint.web.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.web.applicationmap.ApplicationMapBuilder;
import com.nhn.pinpoint.web.vo.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;
import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.dao.AgentInfoDao;
import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.nhn.pinpoint.web.dao.TraceDao;
import com.nhn.pinpoint.web.filter.Filter;

/**
 * @author netspider
 * @author emeroad
 */
@Service
public class FilteredApplicationMapServiceImpl implements FilteredApplicationMapService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TraceDao traceDao;

	@Autowired
	private ApplicationIndexDao applicationIndexDao;

	@Autowired
	private ApplicationTraceIndexDao applicationTraceIndexDao;

	@Autowired
	private AgentInfoDao agentInfoDao;

	private static final Object V = new Object();

	@Override
	public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, Range range, int limit) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (logger.isTraceEnabled()) {
			logger.trace("scan(selectTraceIdsFromApplicationTraceIndex) {}, {}", applicationName, range);
		}

		return this.applicationTraceIndexDao.scanTraceIndex(applicationName, range, limit);
	}

	@Override
	public com.nhn.pinpoint.web.vo.LinkStatistics linkStatistics(Range range, List<TransactionId> traceIdSet, Application sourceApplication, Application destinationApplication, Filter filter) {
        if (sourceApplication == null) {
            throw new NullPointerException("sourceApplication must not be null");
        }
        if (destinationApplication == null) {
            throw new NullPointerException("destApplicationName must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }

        StopWatch watch = new StopWatch();
		watch.start();

		List<List<SpanBo>> originalList = this.traceDao.selectAllSpans(traceIdSet);
        List<SpanBo> filteredTransactionList = filterList(originalList, filter);

		com.nhn.pinpoint.web.vo.LinkStatistics statistics = new com.nhn.pinpoint.web.vo.LinkStatistics(range);

		// TODO fromToFilter처럼. node의 타입에 따른 처리 필요함.

		// scan transaction list
		for (SpanBo span : filteredTransactionList) {
			if (sourceApplication.equals(span.getApplicationId(), span.getServiceType())) {
				List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
				if (spanEventBoList == null) {
					continue;
				}

				// find dest elapsed time
				for (SpanEventBo spanEventBo : spanEventBoList) {
                    if (destinationApplication.equals(spanEventBo.getDestinationId(), spanEventBo.getServiceType())) {
						// find exception
						boolean hasException = spanEventBo.hasException();
						// add sample
						// TODO : 실제값 대신 slot값을 넣어야 함.
						statistics.addSample(span.getStartTime() + spanEventBo.getStartElapsed(), spanEventBo.getEndElapsed(), 1, hasException);
						break;
					}
				}
			}
		}

		watch.stop();
		logger.info("Fetch link statistics elapsed. {}ms", watch.getLastTaskTimeMillis());

		return statistics;
	}

    private List<SpanBo> filterList(List<List<SpanBo>> transactionList, Filter filter) {
        final List<SpanBo> filteredResult = new ArrayList<SpanBo>();
        for (List<SpanBo> transaction : transactionList) {
            if (filter.include(transaction)) {
                filteredResult.addAll(transaction);
            }
        }
        return filteredResult;
    }

    private List<List<SpanBo>> filterList2(List<List<SpanBo>> transactionList, Filter filter) {
        final List<List<SpanBo>> filteredResult = new ArrayList<List<SpanBo>>();
        for (List<SpanBo> transaction : transactionList) {
            if (filter.include(transaction)) {
                filteredResult.add(transaction);
            }
        }
        return filteredResult;
    }

    @Override
	public ApplicationMap selectApplicationMap(TransactionId transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        List<TransactionId> transactionIdList = new ArrayList<TransactionId>();
		transactionIdList.add(transactionId);
		// FIXME from,to -1 땜방임.
        Range range = new Range(-1, -1);
        return selectApplicationMap(transactionIdList, range, Filter.NONE);
	}

	/**
	 * filtered application map
	 */
	@Override
	public ApplicationMap selectApplicationMap(List<TransactionId> transactionIdList, Range range, Filter filter) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }

        StopWatch watch = new StopWatch();
		watch.start();

		// 개별 객체를 각각 보고 재귀 내용을 삭제함.
		// 향후 tree base로 충돌구간을 점검하여 없앨 경우 여기서 filter를 치면 안됨.
		final Collection<TransactionId> recursiveFilterList = recursiveCallFilter(transactionIdList);
		// FIXME 나중에 List<Span>을 순회하면서 실행할 process chain을 두는것도 괜찮을듯.
		final List<List<SpanBo>> originalList = this.traceDao.selectAllSpans(recursiveFilterList);
        final List<List<SpanBo>> filterList = filterList2(originalList, filter);

        Set<com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics> statisticsData = new HashSet<com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics>();
		Map<NodeId, com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics> statisticsMap = new HashMap<NodeId, com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics>();


		final TimeSeriesStore timeSeriesStore = new TimeSeriesStoreImpl2(range);
		/**
		 * 통계정보로 변환한다.
		 */
		for (List<SpanBo> transaction : filterList) {
            final Map<Long, SpanBo> transactionSpanMap = new HashMap<Long, SpanBo>(transactionIdList.size());
			for (SpanBo span : transaction) {
                final SpanBo old = transactionSpanMap.put(span.getSpanId(), span);
                if (old != null) {
                    logger.warn("duplicated span found:{}", old);
                }
            }

			for (SpanBo span : transaction) {
                final Node srcNode = createNode(span, transactionSpanMap);
                final Node destNode = new Node(span.getApplicationId(), span.getServiceType());
                // record해야 되거나. rpc콜은 링크이다.
                if (!destNode.getServiceType().isRecordStatistics() || destNode.getServiceType().isRpcClient()) {
					continue;
				}

                final ComplexNodeId statId = new ComplexNodeId(srcNode, destNode);
				com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics stat = statisticsMap.get(statId);
                if (stat == null)  {
                    Application source = new Application(srcNode.getName(), srcNode.getServiceType());
                    Application dest = new Application(destNode.getName(), destNode.getServiceType());
                    stat = new com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics(source, dest);
                }

                final short slot = getHistogramSlotTime(span, destNode.getServiceType());

                stat.addSample(destNode.getName(), destNode.getServiceType().getCode(), (short) slot, 1);

				statisticsData.add(stat);
				statisticsMap.put(statId, stat);

				// link timeseries statistics추가.
				timeSeriesStore.add(statId, span.getCollectorAcceptTime(), slot, 1L, span.hasException());
				
				// application timeseries statistics
                NodeId key = new ComplexNodeId(Node.EMPTY, new Node(span.getApplicationId(), span.getServiceType()));
				timeSeriesStore.add(key, span.getCollectorAcceptTime(), slot, 1L, span.hasException());

                addNodeFromSpanEvent(statisticsData, statisticsMap, timeSeriesStore, transactionSpanMap, span);
            }
		}

		// mark agent info
		for (com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics stat : statisticsData) {
			fillAdditionalInfo(stat);
		}

        ApplicationMap map = new ApplicationMapBuilder().build(statisticsData);
		map.setTimeSeriesStore(timeSeriesStore);

		watch.stop();
		logger.debug("Select filtered application map elapsed. {}ms", watch.getTotalTimeMillis());

		return map;
	}




    private void addNodeFromSpanEvent(Set<com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics> statisticsData, Map<NodeId, com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics> statisticsMap, TimeSeriesStore timeSeriesStore, Map<Long, SpanBo> transactionSpanMap, SpanBo span) {
        /**
         * span event의 statistics추가.
         */
        final List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return;
        }
        final Node srcNode = new Node(span.getApplicationId(), span.getServiceType());

        for (SpanEventBo spanEvent : spanEventBoList) {
            final String dest = spanEvent.getDestinationId();
            ServiceType destServiceType = spanEvent.getServiceType();

            if (!destServiceType.isRecordStatistics() /*|| destServiceType.isRpcClient()*/) {
                continue;
            }

            // rpc client이면서 acceptor가 없으면 unknown으로 변환시킨다.
            // 내가 아는 next spanid를 spanid로 가진 span이 있으면 acceptor가 존재하는 셈.
            if (destServiceType.isRpcClient()) {
                if (transactionSpanMap.containsKey(spanEvent.getNextSpanId())) {
                    // TODO 여기를 고칠것.  rpc콜의 통계는 별도로 분리해서 생성해야 한다. 치환하면 안됨.
                    continue;
                } else {
                    destServiceType = ServiceType.UNKNOWN; // ServiceType.UNKNOWN_CLOUD;
                }
            }

            final NodeId spanEventStatId = new ComplexNodeId(srcNode, new Node(dest, destServiceType));
            com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics statistics = statisticsMap.get(spanEventStatId);
            if (statistics == null) {
                Application sourceApplication = new Application(srcNode.getName(), srcNode.getServiceType());
                Application destApplication = new Application(dest, destServiceType);
                statistics = new com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics(sourceApplication, destApplication);
            }

            final int slot2 = getHistogramSlotTime(spanEvent, destServiceType);

            // FIXME
            // stat2.addSample((dest == null) ? spanEvent.getEndPoint() : dest, destServiceType.getCode(), (short) slot2, 1);
            statistics.addSample(spanEvent.getEndPoint(), destServiceType.getCode(), (short) slot2, 1);

            // agent 정보추가. destination의 agent정보 알 수 없음.
            statisticsData.add(statistics);
            statisticsMap.put(spanEventStatId, statistics);

            // link timeseries statistics추가.
            timeSeriesStore.add(spanEventStatId, span.getStartTime() + spanEvent.getStartElapsed(), slot2, 1L, spanEvent.hasException());

            // application timeseries statistics
            NodeId key = new ComplexNodeId(Node.EMPTY, new Node(spanEvent.getDestinationId(), spanEvent.getServiceType()));
            timeSeriesStore.add(key, span.getCollectorAcceptTime(), slot2, 1L, spanEvent.hasException());
        }
    }

    private Node createNode(SpanBo span, Map<Long, SpanBo> transactionSpanMap) {
        final SpanBo parentSpan = transactionSpanMap.get(span.getParentSpanId());
        if (span.isRoot() || parentSpan == null) {
            String src = span.getApplicationId();
            ServiceType srcServiceType = ServiceType.USER; // ServiceType.CLIENT;
            return new Node(src, srcServiceType);
        } else {
            String src = parentSpan.getApplicationId();
            ServiceType serviceType = parentSpan.getServiceType();
            return new Node(src, serviceType);
        }
    }

    private short getHistogramSlotTime(SpanEventBo spanEvent, ServiceType serviceType) {
        return getHistogramSlotTime(spanEvent.hasException(), spanEvent.getEndElapsed(), serviceType);
    }

    private short getHistogramSlotTime(SpanBo span, ServiceType serviceType) {
        boolean allException = span.getErrCode() != 0;
        return getHistogramSlotTime(allException, span.getElapsed(), serviceType);
    }

    private short getHistogramSlotTime(boolean hasException, int elapsedTime, ServiceType serviceType) {
        if (hasException) {
            return HistogramSchema.ERROR_SLOT.getSlotTime();
        } else {
            final HistogramSchema schema = serviceType.getHistogramSchema();
            final HistogramSlot histogramSlot = schema.findHistogramSlot(elapsedTime);
            return histogramSlot.getSlotTime();
        }
    }

    private Collection<TransactionId> recursiveCallFilter(List<TransactionId> transactionIdList) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }

        List<TransactionId> crashKey = new ArrayList<TransactionId>();
		Map<TransactionId, Object> filterMap = new LinkedHashMap<TransactionId, Object>(transactionIdList.size());
		for (TransactionId transactionId : transactionIdList) {
			Object old = filterMap.put(transactionId, V);
			if (old != null) {
				crashKey.add(transactionId);
			}
		}
		if (crashKey.size() != 0) {
			Set<TransactionId> filteredTrasnactionId = filterMap.keySet();
			logger.info("transactionId crash found. original:{} filter:{} crashKey:{}", transactionIdList.size(), filteredTrasnactionId.size(), crashKey);
			return filteredTrasnactionId;
		}
		return transactionIdList;
	}

	private void fillAdditionalInfo(com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics stat) {
		if (stat.getToServiceType().isTerminal() || stat.getToServiceType().isUnknown()) {
			return;
		}
		Set<AgentInfoBo> agentSet = selectAgents(stat.getToApplication().getName());
		if (agentSet.isEmpty()) {
			return;
		}
		// destination이 WAS이고 agent가 설치되어있으면 agentSet이 존재한다.
		stat.addToAgentSet(agentSet);
		logger.debug("fill agent info. {}, {}", stat.getTo(), agentSet);
	}

	private Set<AgentInfoBo> selectAgents(String applicationId) {
		String[] agentIds = applicationIndexDao.selectAgentIds(applicationId);
		Set<AgentInfoBo> agentSet = new HashSet<AgentInfoBo>();
		for (String agentId : agentIds) {
			// TODO 조회 시간대에 따라서 agent info row timestamp를 변경하여 조회해야하는지는 모르겠음.
			AgentInfoBo info = agentInfoDao.findAgentInfoBeforeStartTime(agentId, System.currentTimeMillis());
			agentSet.add(info);
		}
		return agentSet;
	}

}
