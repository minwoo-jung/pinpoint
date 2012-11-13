package com.profiler.context;

import com.profiler.common.util.AnnotationTranscoder;
import com.profiler.common.util.AnnotationTranscoder.Encoded;
import com.profiler.sender.DataSender;
import com.profiler.sender.LoggingDataSender;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author netspider
 */
public final class Trace {

    private final Logger logger = Logger.getLogger(Trace.class.getName());

    private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();
    private static final DataSender DEFULT_DATA_SENDER = new LoggingDataSender();

    public static final int HANDLER_STACKID = -2;
    public static final int NOCHECK_STACKID = -1;
    public static final int ROOT_STACKID = 0;

    //    private static final DeadlineSpanMap spanMap = new DeadlineSpanMap();
    private boolean tracingEnabled = true;

    private TraceID root;
    private CallStack callStack;

    private DataSender dataSender = DEFULT_DATA_SENDER;

    public Trace() {
        // traceObject에서 spanid의 유효성을 히스토리를 관리한다면 같은 thread에서는 span랜덤생성아이디의 충돌을 방지할수 있기는 함.
        this.root = TraceID.newTraceId();
        this.callStack = new CallStack();
        StackFrame stackFrame = createStackFrame(root, ROOT_STACKID);
        this.callStack.setStackFrame(stackFrame);
    }

    public Trace(TraceID continueRoot) {
        this.root = continueRoot;
        this.callStack = new CallStack();
        StackFrame stackFrame = createStackFrame(continueRoot, ROOT_STACKID);
        this.callStack.setStackFrame(stackFrame);
    }

    public DataSender getDataSender() {
        return dataSender;
    }

    public void setDataSender(DataSender dataSender) {
        this.dataSender = dataSender;
    }

    public void handle(TraceHandler handler) {
        try {
            TraceID nextId = getNextTraceId();
            callStack.push();
            StackFrame stackFrame = createStackFrame(nextId, HANDLER_STACKID);
            callStack.setStackFrame(stackFrame);
            handler.handle(nextId);
        } finally {
            // stackID check하면 좋을듯.
            callStack.pop();
        }
    }

    private StackFrame createStackFrame(TraceID nextId, int stackId) {
        StackFrame stackFrame = new StackFrame();
        stackFrame.setStackFrameId(stackId);
        stackFrame.setTraceID(nextId);

        Span span = new Span(nextId, null, null);
        stackFrame.setSpan(span);
        return stackFrame;
    }

    public void traceBlockBegin() {
        traceBlockBegin(NOCHECK_STACKID);
    }

    public void markBeforeTime() {
        StackFrame context = getCurrentStackFrame();
        context.markBeforeTime();
    }

    public long afterTime() {
        StackFrame context = getCurrentStackFrame();
        return context.afterTime();
    }

    public void traceBlockBegin(int stackId) {
        TraceID nextId = getNextTraceId();
        callStack.push();
        StackFrame stackFrame = createStackFrame(nextId, stackId);
        callStack.setStackFrame(stackFrame);
    }

    public void traceBlockEnd() {
        traceBlockEnd(NOCHECK_STACKID);
    }

    public void traceBlockEnd(int stackId) {
        StackFrame currentStackFrame = callStack.getCurrentStackFrame();
        if (currentStackFrame.getStackFrameId() != stackId) {
            // 자체 stack dump를 하면 오류발견이 쉬울것으로 생각됨.
            logger.warning("Corrupted CallStack found. StackId not matched");
        }
        callStack.pop();
    }

    public StackFrame getCurrentStackFrame() {
        return callStack.getCurrentStackFrame();
    }

    public boolean removeCurrentTraceIdFromStack() {
        StackFrame currentStackFrame = callStack.getCurrentStackFrame();
        if (currentStackFrame != null) {
            TraceID traceId = currentStackFrame.getTraceID();
            callStack.currentStackFrameClear();
//            spanMap.remove(traceId);
            return true;
        }
        return false;
    }

    /**
     * Get current TraceID. If it was not set this will return null.
     *
     * @return
     */
    public TraceID getCurrentTraceId() {
        return callStack.getCurrentStackFrame().getTraceID();
    }

    public void enable() {
        tracingEnabled = true;
    }

    public void disable() {
        tracingEnabled = false;
    }

    public TraceID getNextTraceId() {
        TraceID current = getCurrentTraceId();
        return current.getNextTraceId();
    }


    private void logSpan(String key, Span span) {
        if (key == null) {
            return;
        }
        if (key.equals(Annotation.ClientRecv.getCode()) || key.equals(Annotation.ServerSend.getCode())) {
            logSpan(span);
        }
    }

    void logSpan(Span span) {
        try {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("[WRITE SPAN]" + span + " CurrentThreadID=" + Thread.currentThread().getId() + ",\n\t CurrentThreadName=" + Thread.currentThread().getName() + "\n\n");
            }

            // TODO: remove this, just for debugging
            // if (spanMap.size() > 0) {
            // System.out.println("##################################################################");
            // System.out.println("# [DEBUG MSG] WARNING SpanMap size > 0 check spanMap.            #");
            // System.out.println("##################################################################");
            // System.out.println("current spamMap=" + spanMap);
            // }

            dataSender.send(span.toThrift());
//            span.cancelTimer();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void record(Annotation annotation) {
        if (!tracingEnabled)
            return;

        annotate(annotation.getCode(), null);
    }

    public void record(Annotation annotation, long duration) {
        if (!tracingEnabled)
            return;

        annotate(annotation.getCode(), duration);
    }

    public void recordAttribute(final String key, final String value) {
        recordAttribute(key, (Object) value);
    }

    public void recordAttribute(final String key, final Object value) {
        if (!tracingEnabled)
            return;

        try {
            Span span = getCurrentStackFrame().getSpan();
            Encoded enc = transcoder.encode(value);
            span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), key, enc.getValueType(), enc.getBytes(), null));
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void recordMessage(String message) {
        if (!tracingEnabled)
            return;

        annotate(message, null);
    }

    public void recordRpcName(final String service, final String rpc) {
        if (!tracingEnabled)
            return;

        try {
            Span span = getCurrentStackFrame().getSpan();
            span.setServiceName(service);
            span.setName(rpc);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void recordTerminalEndPoint(final String endPoint) {
        recordEndPoint(endPoint, true);
    }

    public void recordEndPoint(final String endPoint) {
        recordEndPoint(endPoint, false);
    }

    // TODO: final String... endPoint로 받으면 합치는데 비용이 들어가 그냥 한번에 받는게 나을것 같음.
    private void recordEndPoint(final String endPoint, final boolean isTerminal) {
        if (!tracingEnabled)
            return;

        try {
            Span span = getCurrentStackFrame().getSpan();
            span.setEndPoint(endPoint);
            span.setTerminal(isTerminal);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void annotate(final String key, final Long duration) {
        if (!tracingEnabled)
            return;

        try {
            Span span = getCurrentStackFrame().getSpan();
            span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), key, duration));
            logSpan(key, span);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }


}