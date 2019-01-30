package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.collector.dao.TraceDao;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanFactory;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;
import com.navercorp.pinpoint.thrift.dto.TAnnotationValue;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import com.navercorp.pinpoint.web.calltree.span.Align;
import com.navercorp.pinpoint.web.util.CallStackVerifier;
import org.apache.thrift.TException;
import org.junit.Assert;
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
import java.util.List;

/**
 * @author emeroad
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class SpanServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private long spanAcceptTime = System.currentTimeMillis();

    private SpanFactory spanFactory;

    @Autowired
    private TraceDao traceDao;

    @Autowired
    private SpanService spanService;

    @Autowired
    private HbaseTemplate2 template2;

    @Autowired
    private AcceptedTimeService acceptedTimeService;

    @Before
    public void before() throws TException {
        spanFactory = new SpanFactory();
        spanFactory.setAcceptedTimeService(acceptedTimeService);
    }

    @Test
    public void testSingleTier() {

        long rootSpanId = 0;
        TSpan rootSpan = createRootSpan(rootSpanId);

        CallStackVerifier callStackVerifier = new CallStackVerifier(rootSpan);

        TransactionId transactionId = TransactionIdUtils.parseTransactionId(rootSpan.getTransactionId());
        logger.debug("id:{}", transactionId);

        TSpan childSpan1 = createChildSpan(rootSpan, 1, (short) 0);
        TSpan childSpan2 = createChildSpan(rootSpan, 2, (short) 1);

        callStackVerifier.addChildSpan(rootSpan, childSpan1);
        callStackVerifier.addChildSpan(rootSpan, childSpan2);

        insert(rootSpan, childSpan1, childSpan2);
        logger.info("rootSpan: {}", rootSpan.toString());
        logger.info("childSpan1: {}", childSpan1.toString());
        logger.info("childSpan2: {}", childSpan2.toString());

        SpanResult spanResult = doRead(rootSpan);
        assertSpanResult(callStackVerifier, spanResult);
    }

    @Test
    public void testMultipleTiers() {

        long rootSpanId = 0;
        TSpan rootSpan = createRootSpan(rootSpanId);

        CallStackVerifier callStackVerifier = new CallStackVerifier(rootSpan);

        TransactionId transactionId = TransactionIdUtils.parseTransactionId(rootSpan.getTransactionId());
        logger.debug("id:{}", transactionId);

        TSpan childSpan1 = createChildSpan(rootSpan, 1, (short) 0);
        TSpan childSpan2 = createChildSpan(rootSpan, 2, (short) 1);
        TSpan childSpan3 = createChildSpan(rootSpan, 3, (short) 2);
        TSpan childSpan1_1 = createChildSpan(childSpan1, 11, (short) 0);
        TSpan childSpan1_2 = createChildSpan(childSpan1, 12, (short) 1);
        TSpan childSpan3_1 = createChildSpan(childSpan3, 31, (short) 0);
        TSpan childSpan1_2_1 = createChildSpan(childSpan1_2, 121, (short) 0);

        callStackVerifier.addChildSpan(rootSpan, childSpan1);
        callStackVerifier.addChildSpan(rootSpan, childSpan2);
        callStackVerifier.addChildSpan(rootSpan, childSpan3);
        callStackVerifier.addChildSpan(childSpan1, childSpan1_1);
        callStackVerifier.addChildSpan(childSpan1, childSpan1_2);
        callStackVerifier.addChildSpan(childSpan3, childSpan3_1);
        callStackVerifier.addChildSpan(childSpan1_2, childSpan1_2_1);

        insert(rootSpan, childSpan1, childSpan2, childSpan3, childSpan1_1, childSpan1_2, childSpan3_1, childSpan1_2_1);
        logger.info("rootSpan: {}", rootSpan.toString());
        logger.info("childSpan1: {}", childSpan1.toString());
        logger.info("childSpan2: {}", childSpan2.toString());
        logger.info("childSpan3: {}", childSpan3.toString());
        logger.info("childSpan1_1: {}", childSpan1_1.toString());
        logger.info("childSpan1_2: {}", childSpan1_2.toString());
        logger.info("childSpan3_1: {}", childSpan3_1.toString());
        logger.info("childSpan1_2_1: {}", childSpan1_2_1.toString());

        SpanResult spanResult = doRead(rootSpan);
        assertSpanResult(callStackVerifier, spanResult);
    }

    private void insert(TSpan... tSpans) {
        acceptedTimeService.accept(this.spanAcceptTime);
        for (TSpan tSpan : tSpans) {
            SpanBo span = spanFactory.buildSpanBo(tSpan);
            traceDao.insert(span);
        }
    }

    private SpanResult doRead(TSpan span) {
        TransactionId transactionId = TransactionIdUtils.parseTransactionId(span.getTransactionId());
        // selectedHint를 좀더 정확히 수정할것.
        return spanService.selectSpan(transactionId, spanAcceptTime);
    }

    private void assertSpanResult(CallStackVerifier callStackVerifier, SpanResult spanResult) {
        List<Align> aligns = spanResult.getCallTree().values();
        List<CallStackVerifier.CallStackElement> verifierElements = callStackVerifier.values();
        Assert.assertEquals(verifierElements.size(), aligns.size());
        for (int i = 0; i < aligns.size(); i++) {
            Align align = aligns.get(i);
            logSpanAlign(align);
            CallStackVerifier.CallStackElement verifierElement = verifierElements.get(i);
            verifierElement.verifySpanAlign(align);
        }
    }

    private void logSpanAlign(Align align) {
        String indent = "  ";
        StringBuilder whitespaceBuilder = new StringBuilder(indent);
        int depth = align.getDepth();
        for (int i = 0; i < depth; i++) {
            whitespaceBuilder.append(indent);
        }
        logger.info("{}{}, spanId:{}, depth:{}, spanAlign:{}",
                whitespaceBuilder.toString(),
                align.isSpan() ? "Span" : "SpanEvent",
                align.getSpanId(), align.getDepth(), align.toString());
    }

    private TSpan createRootSpan(long spanId) {
        // 별도 생성기로 뽑을것.
        List<TAnnotation> ano = Collections.emptyList();
        long time = this.spanAcceptTime;

        TSpan span = new TSpan();

        span.setAgentId("UnitTest");
        span.setApplicationName("ApplicationId");
        byte[] bytes = TransactionIdUtils.formatBytes("traceAgentId", System.currentTimeMillis(), 0);
        span.setTransactionId(bytes);
        span.setSpanId(spanId);

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

    private TSpan createChildSpan(TSpan parentSpan, long childSpanId, short sequence) {
        List<TAnnotation> ano = Collections.emptyList();
        long time = System.currentTimeMillis();

        TSpanEvent clientSpanEvent = new TSpanEvent();
        clientSpanEvent.setNextSpanId(childSpanId);
        clientSpanEvent.setDepth(1);
        clientSpanEvent.setSequence(sequence);
        parentSpan.addToSpanEventList(clientSpanEvent);

        TSpan childSpan = new TSpan();

        childSpan.setSpanId(childSpanId);
        childSpan.setAgentId("UnitTest");
        childSpan.setApplicationName("ApplicationId");
        childSpan.setAgentStartTime(123);

        childSpan.setTransactionId(parentSpan.getTransactionId());

        childSpan.setStartTime(time);
        childSpan.setElapsed(5);
        childSpan.setRpc("RPC");
        childSpan.setServiceType(ServiceType.UNKNOWN.getCode());
        childSpan.setAnnotations(ano);

        childSpan.setParentSpanId(parentSpan.getSpanId());
        List<TAnnotation> annotations = new ArrayList<>();
        TAnnotation annotation = new TAnnotation(AnnotationKey.API.getCode());
        annotation.setValue(TAnnotationValue.stringValue(""));
        annotations.add(annotation);
        childSpan.setAnnotations(annotations);

        return childSpan;
    }
}
