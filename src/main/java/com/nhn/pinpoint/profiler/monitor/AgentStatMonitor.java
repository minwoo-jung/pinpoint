package com.nhn.pinpoint.profiler.monitor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.nhn.pinpoint.profiler.monitor.codahale.gc.GarbageCollector;
import com.nhn.pinpoint.profiler.sender.DataSender;
import com.nhn.pinpoint.thrift.dto.AgentInfo;
import com.nhn.pinpoint.thrift.dto.AgentStat;

/**
 * AgentStat monitor
 * 
 * @author harebox
 * 
 */
public class AgentStatMonitor {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final long DEFAULT_INTERVAL = 1000 * 5;
	
	private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new PinpointThreadFactory("Pinpoint-stat-monitor", true));

	private DataSender dataSender;
	private AgentInfo agentInfo;
	private TraceContext traceContext;
	private final ProfilerConfig profilerConfig;

	public AgentStatMonitor(TraceContext traceContext, ProfilerConfig profilerConfig) {
		if (traceContext == null) {
			throw new NullPointerException("traceContext is null");
		}
		this.traceContext = traceContext;
		this.profilerConfig = profilerConfig;
	}

	public void setDataSender(DataSender dataSender) {
		this.dataSender = dataSender;
	}
	
	public void setAgentInfo(AgentInfo agentInfo) {
		this.agentInfo = agentInfo;
	}

	public void start() {
		CollectJob job = new CollectJob(dataSender, traceContext, profilerConfig);
		// FIXME 설정에서 수집 주기를 가져올 수 있어야 한다.
		long interval = DEFAULT_INTERVAL;
		long wait = 0;
		
		executor.scheduleAtFixedRate(job, wait, interval, TimeUnit.MILLISECONDS);
		logger.info("AgentStat monitor started");
	}

	public void shutdown() {
		executor.shutdown();
		logger.info("AgentStat monitor stopped");
	}

	class CollectJob implements Runnable {
		private AgentStat agentStat;
		private DataSender dataSender;
		private TraceContext traceContext;
		private ProfilerConfig profilerConfig;
		private MetricMonitorRegistry monitorRegistry;
		private GarbageCollector garbageCollector;

		public CollectJob(DataSender dataSender, TraceContext traceContext, ProfilerConfig profilerConfig) {
			this.dataSender = dataSender;
			this.traceContext = traceContext;
			this.profilerConfig = profilerConfig;
			
			// FIXME 디폴트 레지스트리를 생성하여 사용한다. 다른데서 쓸 일이 있으면 외부에서 삽입하도록 하자.
			this.monitorRegistry = new MetricMonitorRegistry();
			
			// FIXME 설정에 따라 어떤 데이터를 수집할 지 선택할 수 있도록 해야한다. 여기서는 JVM 메모리 정보를 default로 수집.
			this.monitorRegistry.registerJvmMemoryMonitor(new MonitorName(MetricMonitorValues.JVM_MEMORY));
			this.monitorRegistry.registerJvmGcMonitor(new MonitorName(MetricMonitorValues.JVM_GC));
			
			if (agentInfo != null) {
				this.agentStat = new AgentStat();
			}
			
			this.garbageCollector = new GarbageCollector();
			this.garbageCollector.setType(monitorRegistry);
			logger.info("found : {}", this.garbageCollector);
		}
		
		public void run() {
			try {
				garbageCollector.map(monitorRegistry, agentStat, agentInfo.getAgentId());

				dataSender.send(agentStat);
			} catch (Exception e) {
				logger.warn("AgentStat collect failed : {}", e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
