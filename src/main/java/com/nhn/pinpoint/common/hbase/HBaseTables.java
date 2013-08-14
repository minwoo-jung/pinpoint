package com.nhn.pinpoint.common.hbase;

import org.apache.hadoop.hbase.util.Bytes;

public class HBaseTables {
	
	public static final int APPLICATION_NAME_MAX_LEN = 24;
    public static final int AGENT_NAME_MAX_LEN = 24;
	
    public static final String SYSTEMINFO = "Systeminfo";
    public static final byte[] SYSTEMINFO_CF_JVM = Bytes.toBytes("JVM");
    public static final byte[] SYSTEMINFO_CN_INFO = Bytes.toBytes("info");

    @Deprecated
    public static final String TRACE_INDEX = "TraceIndex";
    @Deprecated
    public static final byte[] TRACE_INDEX_CF_TRACE = Bytes.toBytes("I"); // Trace
    @Deprecated
    public static final int TRACE_INDEX_ROW_DISTRIBUTE_SIZE = 1; // Trace_Index hash size

    public static final String APPLICATION_TRACE_INDEX = "ApplicationTraceIndex";
    public static final byte[] APPLICATION_TRACE_INDEX_CF_TRACE = Bytes.toBytes("I"); // applicationIndex
    public static final int APPLICATION_TRACE_INDEX_ROW_DISTRIBUTE_SIZE = 1; // applicationIndex hash size

    public static final String TRACES = "Traces";
    public static final byte[] TRACES_CF_SPAN = Bytes.toBytes("S");  //Span
    public static final byte[] TRACES_CF_ANNOTATION = Bytes.toBytes("A");  //Annotation
    public static final byte[] TRACES_CF_TERMINALSPAN = Bytes.toBytes("T"); //TerminalSpan

    @Deprecated
    public static final String SERVERS_INDEX = "ServersIndex";
    @Deprecated
    public static final byte[] SERVERS_INDEX_CF_AGENTS = Bytes.toBytes("Agents");

    public static final String APPLICATION_INDEX = "ApplicationIndex";
    public static final byte[] APPLICATION_INDEX_CF_AGENTS = Bytes.toBytes("Agents");

    public static final String AGENTINFO = "AgentInfo";
    public static final byte[] AGENTINFO_CF_INFO = Bytes.toBytes("Info");
    public static final byte[] AGENTINFO_CF_INFO_IDENTIFIER = Bytes.toBytes("i");

    public static final String AGENTID_APPLICATION_INDEX = "AgentIdApplicationIndex";
    public static final byte[] AGENTID_APPLICATION_INDEX_CF_APPLICATION = Bytes.toBytes("Application");

    @Deprecated
    public static final String TERMINAL_STATISTICS = "TerminalStatistics";
    @Deprecated
    public static final byte[] TERMINAL_STATISTICS_CF_COUNTER = Bytes.toBytes("Counter");
    @Deprecated
	public static final byte[] TERMINAL_STATISTICS_CQ_ERROR_SLOT = Bytes.toBytes((short) -1);

	@Deprecated
	public static final String CLIENT_STATISTICS = "ClientStatistics";
	@Deprecated
	public static final byte[] CLIENT_STATISTICS_CF_COUNTER = Bytes.toBytes("Counter");
	@Deprecated
	public static final byte[] CLIENT_STATISTICS_CQ_ERROR_SLOT = Bytes.toBytes((short) -1);

	@Deprecated
	public static final String BUSINESS_TRANSACTION_STATISTICS = "BusinessTransactionStatistics";
	@Deprecated
	public static final byte[] BUSINESS_TRANSACTION_STATISTICS_CF_NORMAL = Bytes.toBytes("Normal");
	@Deprecated
	public static final byte[] BUSINESS_TRANSACTION_STATISTICS_CF_SLOW = Bytes.toBytes("Slow");
	@Deprecated
	public static final byte[] BUSINESS_TRANSACTION_STATISTICS_CF_ERROR = Bytes.toBytes("Error");

    public static final String SQL_METADATA = "SqlMetaData";
    public static final byte[] SQL_METADATA_CF_SQL = Bytes.toBytes("Sql");

    public static final String API_METADATA = "ApiMetaData";
    public static final byte[] API_METADATA_CF_API = Bytes.toBytes("Api");

    public static final String APPLICATION_STATISTICS = "ApplicationStatistics";
    public static final byte[] APPLICATION_STATISTICS_CF_COUNTER = Bytes.toBytes("C");
    
	public static final String APPLICATION_MAP_STATISTICS_CALLER = "ApplicationMapStatisticsCaller";
	public static final byte[] APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER = Bytes.toBytes("C");

	public static final String APPLICATION_MAP_STATISTICS_CALLEE = "ApplicationMapStatisticsCallee";
	public static final byte[] APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER = Bytes.toBytes("C");
	
	public static final String HOST_APPLICATION_MAP = "HostApplicationMap";
	public static final byte[] HOST_APPLICATION_MAP_CF_MAP = Bytes.toBytes("M");
	
	public static final byte[] STATISTICS_CQ_ERROR_SLOT = Bytes.toBytes((short) -1);
}
