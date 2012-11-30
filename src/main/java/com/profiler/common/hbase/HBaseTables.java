package com.profiler.common.hbase;

import org.apache.hadoop.hbase.util.Bytes;

public class HBaseTables {
	
	public static final String SYSTEMINFO = "Systeminfo";
	public static final byte[] SYSTEMINFO_CF_JVM = Bytes.toBytes("JVM");
	public static final byte[] SYSTEMINFO_CN_INFO = Bytes.toBytes("info");
	
	public static final String TRACE_INDEX = "TraceIndex";
	public static final byte[] TRACE_INDEX_CF_TRACE = Bytes.toBytes("Trace");
	public static final byte[] TRACE_INDEX_CN_ID = Bytes.toBytes("ID");

	public static final String APPLICATION_TRACE_INDEX = "ApplicationTraceIndex";
	public static final byte[] APPLICATION_TRACE_INDEX_CF_TRACE = Bytes.toBytes("Trace");
	public static final byte[] APPLICATION_TRACE_INDEX_CN_ID = Bytes.toBytes("ID");

	public static final String ROOT_TRACE_INDEX = "RootTraceIndex";
	public static final byte[] ROOT_TRACE_INDEX_CF_TRACE = Bytes.toBytes("Trace");
	public static final byte[] ROOT_TRACE_INDEX_CN_ID = Bytes.toBytes("ID");

	public static final String TRACES = "Traces";
	public static final byte[] TRACES_CF_SPAN = Bytes.toBytes("Span");
	public static final byte[] TRACES_CF_ANNOTATION = Bytes.toBytes("Annotation");
	public static final byte[] TRACES_CF_TERMINALSPAN = Bytes.toBytes("TerminalSpan");

	public static final String SERVERS_INDEX = "ServersIndex";
	public static final byte[] SERVERS_INDEX_CF_AGENTS = Bytes.toBytes("Agents");

	public static final String APPLICATION_INDEX = "ApplicationIndex";
	public static final byte[] APPLICATION_INDEX_CF_AGENTS = Bytes.toBytes("Agents");

	public static final String AGENTID_APPLICATION_INDEX = "AgentIdApplicationIndex";
	public static final byte[] AGENTID_APPLICATION_INDEX_CF_APPLICATION = Bytes.toBytes("Application");
	
	public static final String TERMINAL_STATISTICS = "TerminalStatistics";
	public static final byte[] TERMINAL_STATISTICS_CF_COUNTER = Bytes.toBytes("Counter");
}
