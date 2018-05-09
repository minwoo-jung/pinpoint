package com.navercorp.pinpoint.web.dao.hbase.filter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.navercorp.pinpoint.common.hbase.HbaseTableNameProvider;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.util.SpanUtils;
import com.navercorp.pinpoint.common.util.TransactionId;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseSystemException;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.HbaseTableFactory;
import com.navercorp.pinpoint.web.mapper.TraceIndexScatterMapper;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseTimeRange;
import com.navercorp.pinpoint.web.vo.SelectedScatterArea;

import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix.OneByteSimpleHash;

public class HbaseFilterPerformanceTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static Connection connection;
    private static HbaseTemplate2 hbaseTemplate2;
    private static AbstractRowKeyDistributor traceIdRowKeyDistributor;
    private static TableNameProvider tableNameProvider = new HbaseTableNameProvider(NamespaceDescriptor.DEFAULT_NAMESPACE_NAME_STR);

    @BeforeClass
    public static void beforeClass() throws IOException {
//        Properties properties = PropertyUtils.loadPropertyFromClassPath("hbase.properties");

        Configuration cfg = HBaseConfiguration.create();
        cfg.set("hbase.zookeeper.quorum", "dev.zk.pinpoint.navercorp.com");
        cfg.set("hbase.zookeeper.property.clientPort", "2181");

        connection = ConnectionFactory.createConnection(cfg);
        hbaseTemplate2 = new HbaseTemplate2();
        hbaseTemplate2.setConfiguration(cfg);
        hbaseTemplate2.setTableFactory(new HbaseTableFactory(connection));
        hbaseTemplate2.afterPropertiesSet();

        OneByteSimpleHash applicationTraceIndexHash = new com.sematext.hbase.wd.RowKeyDistributorByHashPrefix.OneByteSimpleHash(32);
        traceIdRowKeyDistributor = new com.sematext.hbase.wd.RowKeyDistributorByHashPrefix(applicationTraceIndexHash);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (hbaseTemplate2 != null) {
            hbaseTemplate2.destroy();
        }
        if (connection != null) {
            connection.close();
        }
    }

    private Scan createScan(String applicationName, Range range) {
        Scan scan = new Scan();
        scan.setCaching(256);

        byte[] bAgent = Bytes.toBytes(applicationName);
        byte[] traceIndexStartKey = SpanUtils.getTraceIndexRowKey(bAgent, range.getFrom());
        byte[] traceIndexEndKey = SpanUtils.getTraceIndexRowKey(bAgent, range.getTo());

        scan.setStartRow(traceIndexEndKey);
        scan.setStopRow(traceIndexStartKey);

        scan.addFamily(HBaseTables.APPLICATION_TRACE_INDEX_CF_TRACE);
        scan.setId("ApplicationTraceIndexScan");

        return scan;
    }

    private Filter makePrefixFilter(SelectedScatterArea area, TransactionId offsetTransactionId, int offsetTransactionElapsed) {
        // filter by response time
        ResponseTimeRange responseTimeRange = area.getResponseTimeRange();
        byte[] responseFrom = Bytes.toBytes(responseTimeRange.getFrom());
        byte[] responseTo = Bytes.toBytes(responseTimeRange.getTo());
        FilterList filterList = new FilterList(Operator.MUST_PASS_ALL);
        filterList.addFilter(new QualifierFilter(CompareOp.GREATER_OR_EQUAL, new BinaryPrefixComparator(responseFrom)));
        filterList.addFilter(new QualifierFilter(CompareOp.LESS_OR_EQUAL, new BinaryPrefixComparator(responseTo)));

        // add offset
        if (offsetTransactionId != null) {
            final Buffer buffer = new AutomaticBuffer(32);
            buffer.putInt(offsetTransactionElapsed);
            buffer.putPrefixedString(offsetTransactionId.getAgentId());
            buffer.putSVLong(offsetTransactionId.getAgentStartTime());
            buffer.putVLong(offsetTransactionId.getTransactionSequence());
            byte[] qualifierOffset = buffer.getBuffer();

            filterList.addFilter(new QualifierFilter(CompareOp.GREATER, new BinaryPrefixComparator(qualifierOffset)));
        }

        return filterList;
    }

    @Test
    @Ignore
    public void usingFilter() throws Exception {
        try {
            long oneday = 60 * 60 * 24 * 1000;
            int fetchLimit = 1000009;
            long timeTo = 1395989385734L;
            long timeFrom = timeTo - oneday;
            int responseTimeFrom = 0;
            int responseTimeTo = 10000;
            SelectedScatterArea area = new SelectedScatterArea(timeFrom, timeTo, responseTimeFrom, responseTimeTo);

            Scan scan = createScan("API.GATEWAY.DEV", area.getTimeRange());

            scan.setFilter(makePrefixFilter(area, null, -1));

            long startTime = System.currentTimeMillis();
            TableName applicationTraceIndexTableName = tableNameProvider.getTableName(HBaseTables.APPLICATION_TRACE_INDEX_STR);
            List<List<Dot>> dotListList = hbaseTemplate2.find(applicationTraceIndexTableName, scan, traceIdRowKeyDistributor, fetchLimit, new TraceIndexScatterMapper());
            logger.debug("elapsed : {}ms", (System.currentTimeMillis() - startTime));
            logger.debug("fetched size : {}", dotListList.size());
        } catch (HbaseSystemException e) {
            e.printStackTrace();
        }

    }
}
