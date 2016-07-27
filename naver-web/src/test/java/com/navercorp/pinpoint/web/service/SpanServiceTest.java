package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.collector.dao.TracesDao;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanFactory;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.util.SpanUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;
import com.navercorp.pinpoint.thrift.dto.TAnnotationValue;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.vo.TransactionId;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class SpanServiceTest {

    @Autowired
    private TracesDao traceDao;

    @Autowired
    private SpanService spanService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseTemplate2 template2;

    private TSpan root;
    private List<TSpan> deleteSpans = new LinkedList<>();
    private long spanAcceptTime = System.currentTimeMillis();

    @Autowired
    private AcceptedTimeService acceptedTimeService;

    private SpanFactory spanFactory;

    @Before
    public void before() throws TException {
        spanFactory = new SpanFactory();
        spanFactory.setAcceptedTimeService(acceptedTimeService);

        TSpan span = createRootSpan();
        com.navercorp.pinpoint.common.util.TransactionId id = TransactionIdUtils.parseTransactionId(span.getTransactionId());
        logger.debug("id:{}", new TransactionId(id.getAgentId(), id.getAgentStartTime(), id.getTransactionSequence()));
        insert(span);
        deleteSpans.add(span);

        TSpan subSpan1 = createSpanEvent(span);
        insert(subSpan1);
        deleteSpans.add(subSpan1);

        TSpan subSpan1_2 = createSpanEvent(span);
        insert(subSpan1_2);
        deleteSpans.add(subSpan1_2);

        TSpan subSpan2 = createSpanEvent(subSpan1);
        insert(subSpan2);
        deleteSpans.add(subSpan2);

        TSpan subSpan3 = createSpanEvent(subSpan1);
        insert(subSpan3);
        deleteSpans.add(subSpan3);

        root = span;
        logger.info(subSpan1.toString());
        logger.info(subSpan1_2.toString());
        logger.info(subSpan2.toString());
        logger.info(subSpan3.toString());

    }

    public void after() {
        List list = new LinkedList();
        for (TSpan span : deleteSpans) {
            Delete delete = new Delete(SpanUtils.getTransactionId(span));
            list.add(delete);
        }
        template2.delete(HBaseTables.TRACES, list);
        deleteSpans.clear();
    }

    @Test
    public void testReadSpan() throws TException {
        doRead(root);
    }

    @Test
    public void testReadSpanAndAnnotation() throws TException {
        doRead(root);
    }

    private void doRead(TSpan span) {
        com.navercorp.pinpoint.common.util.TransactionId id = TransactionIdUtils.parseTransactionId(span.getTransactionId());
        TransactionId traceId = new TransactionId(id.getAgentId(), id.getAgentStartTime(), id.getTransactionSequence());
        // selectedHint를 좀더 정확히 수정할것.
        SpanResult spanResult = spanService.selectSpan(traceId, spanAcceptTime);
        CallTreeIterator iterator = spanResult.getCallTree();
        for (SpanAlign spanAlign : iterator.values()) {
            logger.info("depth:{} {}", spanAlign.getDepth(), spanAlign.getSpanBo());
        }
        // reorder(spans);
    }

    private void insert(TSpan tSpan) throws TException {
        acceptedTimeService.accept(this.spanAcceptTime);
        SpanBo span = spanFactory.buildSpanBo(tSpan);
        traceDao.insert(span);
    }

    AtomicInteger id = new AtomicInteger(0);

    private TSpan createRootSpan() {
        // 별도 생성기로 뽑을것.
        List<TAnnotation> ano = Collections.emptyList();
        long time = this.spanAcceptTime;
        int andIncrement = id.getAndIncrement();

        TSpan span = new TSpan();

        span.setAgentId("UnitTest");
        span.setApplicationName("ApplicationId");
        byte[] bytes = TransactionIdUtils.formatBytes("traceAgentId", System.currentTimeMillis(), 0);
        span.setTransactionId(bytes);

        span.setStartTime(time);
        span.setElapsed(5);
        span.setRpc("RPC");

        span.setServiceType(ServiceType.UNKNOWN.getCode());
        span.setAnnotations(ano);

        span.setParentSpanId(-1);
        List<TAnnotation> annotations = new ArrayList<>();
        TAnnotation annotation = new TAnnotation(AnnotationKey.API.getCode());
        annotation.setValue(TAnnotationValue.stringValue(""));
        annotations.add(annotation);
        span.setAnnotations(annotations);
        return span;
    }

    private TSpan createSpanEvent(TSpan span) {
        List<TAnnotation> ano = Collections.emptyList();
        long time = System.currentTimeMillis();
        int andIncrement = id.getAndIncrement();

        TSpan sub = new TSpan();

        sub.setAgentId("UnitTest");
        sub.setApplicationName("ApplicationId");
        sub.setAgentStartTime(123);

        sub.setTransactionId(span.getTransactionId());

        sub.setStartTime(time);
        sub.setElapsed(5);
        sub.setRpc("RPC");
        sub.setServiceType(ServiceType.UNKNOWN.getCode());
        sub.setAnnotations(ano);

        sub.setParentSpanId(span.getSpanId());
        List<TAnnotation> annotations = new ArrayList<>();
        TAnnotation annotation = new TAnnotation(AnnotationKey.API.getCode());
        annotation.setValue(TAnnotationValue.stringValue(""));
        annotations.add(annotation);
        sub.setAnnotations(annotations);
        return sub;
    }

}
