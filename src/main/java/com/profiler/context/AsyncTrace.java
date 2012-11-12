package com.profiler.context;

import com.profiler.common.util.AnnotationTranscoder;
import com.profiler.sender.DataSender;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class AsyncTrace {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    // 일단 c&p
    private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();
    //    private int id;
    // 비동기일 경우 traceenable의 경우 애매함. span을 보내는것으로 데이터를 생성하므로 약간 이상.
//    private boolean tracingEnabled;
    private Span span;
    private DataSender dataSender;

    public AsyncTrace(Span span) {
        this.span = span;
    }

    public void record(Annotation annotation) {
        annotate(annotation.getCode(), null);
    }

    public void record(Annotation annotation, long duration) {
        annotate(annotation.getCode(), duration);
    }

    public void recordAttribute(final String key, final String value) {
        recordAttibute(key, (Object) value);
    }

    public void recordAttibute(final String key, final Object value) {
        try {
            // TODO 사용자 thread에서 encoding을 하지 않도록 변경.
            AnnotationTranscoder.Encoded enc = transcoder.encode(value);
            span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), key, enc.getValueType(), enc.getBytes(), null));
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void recordMessage(String message) {
        annotate(message, null);
    }

    public void recordRpcName(final String service, final String rpc) {

        try {
            spanUpdate(new SpanUpdater() {
                @Override
                public Span updateSpan(Span span) {
                    span.setServiceName(service);
                    span.setName(rpc);
                    return span;
                }
            });
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
        try {
            spanUpdate(new SpanUpdater() {
                @Override
                public Span updateSpan(Span span) {
                    // set endpoint to both span and annotations
                    span.setEndPoint(endPoint);
                    span.setTerminal(isTerminal);
                    return span;
                }
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void annotate(final String key, final Long duration) {

        try {
            spanUpdate(new SpanUpdater() {
                @Override
                public Span updateSpan(Span span) {
                    span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), key, duration));
                    return span;
                }
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }


    private void spanUpdate(SpanUpdater spanUpdater) {
        if (span.isExistsAnnotationKey(Annotation.ClientRecv.getCode()) || span.isExistsAnnotationKey(Annotation.ServerSend.getCode())) {
            logSpan(span);
        }
    }

    void logSpan(Span span) {
        try {
            if (logger.isLoggable(Level.INFO)) {
                Thread thread = Thread.currentThread();
                logger.info("[WRITE SPAN]" + span + " CurrentThreadID=" + thread.getId() + ",\n\t CurrentThreadName=" + thread.getName());
            }

            this.dataSender.send(span.toThrift());
//            span.cancelTimer();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
