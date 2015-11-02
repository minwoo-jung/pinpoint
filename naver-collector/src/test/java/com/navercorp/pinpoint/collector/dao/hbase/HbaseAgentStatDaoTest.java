package com.navercorp.pinpoint.collector.dao.hbase;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static com.navercorp.pinpoint.common.hbase.HBaseTables.*;

import java.util.List;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.navercorp.pinpoint.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.collector.dao.hbase.HbaseAgentStatDao;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TCpuLoad;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;
import com.navercorp.pinpoint.thrift.dto.TTransaction;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

/**
 * @author hyungil.jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class HbaseAgentStatDaoTest {

    @Mock
    private HbaseOperations2 hbaseTemplate;

    @Spy
    @Autowired
    @Qualifier("agentStatRowKeyDistributor")
    private AbstractRowKeyDistributor rowKeyDistributor;

    @InjectMocks
    private AgentStatDao agentStatDao = new HbaseAgentStatDao();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_with_only_jvm_gc_set() {
        // Given
        final String agentId = "agentId";
        final long timestamp = Long.MAX_VALUE;
        final TJvmGc jvmGc = createTJvmGc();
        final TAgentStat agentStat = createAgentStat(agentId, timestamp);
        agentStat.setGc(jvmGc);
        final ArgumentCaptor<Put> argCaptor = ArgumentCaptor.forClass(Put.class);
        // When
        agentStatDao.insert(agentStat);
        // Then
        verify(hbaseTemplate).put(eq(AGENT_STAT), argCaptor.capture());
        Put actualPut = argCaptor.getValue();
        List<Cell> cellsToPut = actualPut.getFamilyCellMap().get(AGENT_STAT_CF_STATISTICS);
        verifyColumnsPresent(cellsToPut, jvmGc);
        verifyColumnsNotPresent(cellsToPut, AGENT_STAT_COL_JVM_CPU, AGENT_STAT_COL_SYS_CPU);
        verifyColumnsNotPresent(cellsToPut,
                AGENT_STAT_COL_TRANSACTION_SAMPLED_NEW, AGENT_STAT_COL_TRANSACTION_SAMPLED_CONTINUATION,
                AGENT_STAT_COL_TRANSACTION_UNSAMPLED_NEW, AGENT_STAT_COL_TRANSACTION_UNSAMPLED_CONTINUATION);
    }

    @Test
    public void test_with_jvm_gc_and_cpu_set() {
        // Given
        final String agentId = "agentId";
        final long timestamp = Long.MAX_VALUE;
        final TJvmGc jvmGc = createTJvmGc();
        final TCpuLoad cpuLoad = createTCpuLoad();
        final TAgentStat agentStat = createAgentStat(agentId, timestamp);
        agentStat.setGc(jvmGc);
        agentStat.setCpuLoad(cpuLoad);
        final ArgumentCaptor<Put> argCaptor = ArgumentCaptor.forClass(Put.class);
        // When
        agentStatDao.insert(agentStat);
        // Then
        verify(hbaseTemplate).put(eq(AGENT_STAT), argCaptor.capture());
        Put actualPut = argCaptor.getValue();
        List<Cell> cellsToPut = actualPut.getFamilyCellMap().get(AGENT_STAT_CF_STATISTICS);
        verifyColumnsPresent(cellsToPut, jvmGc);
        verifyColumnsPresent(cellsToPut, cpuLoad);
        verifyColumnsNotPresent(cellsToPut,
                AGENT_STAT_COL_TRANSACTION_SAMPLED_NEW, AGENT_STAT_COL_TRANSACTION_SAMPLED_CONTINUATION,
                AGENT_STAT_COL_TRANSACTION_UNSAMPLED_NEW, AGENT_STAT_COL_TRANSACTION_UNSAMPLED_CONTINUATION);
    }

    @Test
    public void test_all_fields_populated() {
        // Given
        final String agentId = "agentId";
        final long timestamp = Long.MAX_VALUE;
        final TJvmGc jvmGc = createTJvmGc();
        final TCpuLoad cpuLoad = createTCpuLoad();
        final TTransaction transaction = createTTransaction();
        final TAgentStat agentStat = createAgentStat(agentId, timestamp);
        agentStat.setGc(jvmGc);
        agentStat.setCpuLoad(cpuLoad);
        agentStat.setTransaction(transaction);
        final ArgumentCaptor<Put> argCaptor = ArgumentCaptor.forClass(Put.class);
        // When
        agentStatDao.insert(agentStat);
        // Then
        verify(hbaseTemplate).put(eq(AGENT_STAT), argCaptor.capture());
        Put actualPut = argCaptor.getValue();
        List<Cell> cellsToPut = actualPut.getFamilyCellMap().get(AGENT_STAT_CF_STATISTICS);
        verifyColumnsPresent(cellsToPut, jvmGc);
        verifyColumnsPresent(cellsToPut, cpuLoad);
        verifyColumnsPresent(cellsToPut, transaction);
    }

    private void verifyColumnsPresent(List<Cell> cells, TJvmGc gc) {
        for (Cell cell : cells) {
            byte[] columnName = CellUtil.cloneQualifier(cell);
            if (Bytes.equals(columnName, AGENT_STAT_COL_GC_TYPE)) {
                assertArrayEquals(CellUtil.cloneValue(cell), Bytes.toBytes(gc.getType().name()));
            } else if (Bytes.equals(columnName, AGENT_STAT_COL_GC_OLD_COUNT)) {
                assertArrayEquals(CellUtil.cloneValue(cell), Bytes.toBytes(gc.getJvmGcOldCount()));
            } else if (Bytes.equals(columnName, AGENT_STAT_COL_GC_OLD_TIME)) {
                assertArrayEquals(CellUtil.cloneValue(cell), Bytes.toBytes(gc.getJvmGcOldTime()));
            } else if (Bytes.equals(columnName, AGENT_STAT_COL_HEAP_USED)) {
                assertArrayEquals(CellUtil.cloneValue(cell), Bytes.toBytes(gc.getJvmMemoryHeapUsed()));
            } else if (Bytes.equals(columnName, AGENT_STAT_COL_HEAP_MAX)) {
                assertArrayEquals(CellUtil.cloneValue(cell), Bytes.toBytes(gc.getJvmMemoryHeapMax()));
            } else if (Bytes.equals(columnName, AGENT_STAT_COL_NON_HEAP_USED)) {
                assertArrayEquals(CellUtil.cloneValue(cell), Bytes.toBytes(gc.getJvmMemoryNonHeapUsed()));
            } else if (Bytes.equals(columnName, AGENT_STAT_COL_NON_HEAP_MAX)) {
                assertArrayEquals(CellUtil.cloneValue(cell), Bytes.toBytes(gc.getJvmMemoryNonHeapMax()));
            }
        }
    }

    private void verifyColumnsPresent(List<Cell> cells, TCpuLoad cpuLoad) {
        for (Cell cell : cells) {
            byte[] columnName = CellUtil.cloneQualifier(cell);
            if (Bytes.equals(columnName, AGENT_STAT_COL_JVM_CPU)) {
                assertArrayEquals(CellUtil.cloneValue(cell), Bytes.toBytes(cpuLoad.getJvmCpuLoad()));
            } else if (Bytes.equals(columnName, AGENT_STAT_COL_SYS_CPU)) {
                assertArrayEquals(CellUtil.cloneValue(cell), Bytes.toBytes(cpuLoad.getSystemCpuLoad()));
            }
        }
    }

    private void verifyColumnsPresent(List<Cell> cells, TTransaction transaction) {
        for (Cell cell : cells) {
            byte[] columnName = CellUtil.cloneQualifier(cell);
            if (Bytes.equals(columnName, AGENT_STAT_COL_TRANSACTION_SAMPLED_NEW)) {
                assertArrayEquals(CellUtil.cloneValue(cell), Bytes.toBytes(transaction.getSampledNewCount()));
            } else if (Bytes.equals(columnName, AGENT_STAT_COL_TRANSACTION_SAMPLED_CONTINUATION)) {
                assertArrayEquals(CellUtil.cloneValue(cell), Bytes.toBytes(transaction.getSampledContinuationCount()));
            } else if (Bytes.equals(columnName, AGENT_STAT_COL_TRANSACTION_UNSAMPLED_NEW)) {
                assertArrayEquals(CellUtil.cloneValue(cell), Bytes.toBytes(transaction.getUnsampledNewCount()));
            } else if (Bytes.equals(columnName, AGENT_STAT_COL_TRANSACTION_UNSAMPLED_CONTINUATION)) {
                assertArrayEquals(CellUtil.cloneValue(cell), Bytes.toBytes(transaction.getUnsampledContinuationCount()));
            }
        }
    }

    private void verifyColumnsNotPresent(List<Cell> cells, byte[]... columns) {
        for (Cell cell : cells) {
            byte[] columnName = CellUtil.cloneQualifier(cell);
            for (byte[] column : columns) {
                assertFalse("Column [" + Bytes.toString(column) + "] should not be present.", Bytes.equals(columnName, column));
            }
        }
    }

    private TAgentStat createAgentStat(String agentId, long timestamp) {
        final TAgentStat agentStat = new TAgentStat();
        agentStat.setAgentId(agentId);
        agentStat.setTimestamp(timestamp);
        return agentStat;
    }

    private TJvmGc createTJvmGc() {
        final TJvmGc jvmGc = new TJvmGc();
        jvmGc.setType(TJvmGcType.G1);
        jvmGc.setJvmMemoryHeapUsed(Long.MIN_VALUE);
        jvmGc.setJvmMemoryHeapMax(Long.MAX_VALUE);
        jvmGc.setJvmMemoryNonHeapUsed(Long.MIN_VALUE);
        jvmGc.setJvmMemoryNonHeapMax(Long.MAX_VALUE);
        jvmGc.setJvmGcOldCount(1L);
        jvmGc.setJvmGcOldTime(1L);
        return jvmGc;
    }

    private TCpuLoad createTCpuLoad() {
        final TCpuLoad cpuLoad = new TCpuLoad();
        cpuLoad.setJvmCpuLoad(Double.MIN_VALUE);
        cpuLoad.setSystemCpuLoad(Double.MAX_VALUE);
        return cpuLoad;
    }

    private TTransaction createTTransaction() {
        final TTransaction transaction = new TTransaction();
        transaction.setSampledNewCount(Long.MAX_VALUE);
        transaction.setSampledContinuationCount(Long.MAX_VALUE);
        transaction.setUnsampledNewCount(Long.MAX_VALUE);
        transaction.setUnsampledContinuationCount(Long.MAX_VALUE);
        return transaction;
    }

}
