package com.profiler.context;


import com.profiler.sender.DataSender;
import com.profiler.sender.LoggingDataSender;
import com.profiler.util.NamedThreadLocal;

import java.util.concurrent.atomic.AtomicInteger;

public class TraceContext {

    private static TraceContext CONTEXT = new TraceContext();

    // initailze관련 생명주기가 뭔가 애매함. 추후 방안을 더 고려해보자.
    public static TraceContext initialize() {
        return CONTEXT = new TraceContext();
    }

    // 얻는 것도 뭔가 모양이 마음에 안듬.
    public static TraceContext getTraceContext() {
        return CONTEXT;
    }

    private ThreadLocal<Trace> threadLocal = new NamedThreadLocal<Trace>("Trace");

    private final ActiveThreadCounter activeThreadCounter = new ActiveThreadCounter();

    // internal stacktrace 추적때 필요한 unique 아이디, activethreadcount의  slow 타임 계산의 위해서도 필요할듯 함.
    private final AtomicInteger transactionId = new AtomicInteger(0);

    private static final DataSender DEFAULT_DATA_SENDER = new LoggingDataSender();

    private DataSender dataSender = DEFAULT_DATA_SENDER;

    private GlobalCallTrace globalCallTrace = new GlobalCallTrace();

    public TraceContext() {
    }

    public Trace currentTraceObject() {
        return threadLocal.get();
    }

    public void attachTraceObject(Trace trace) {
        Trace old = this.threadLocal.get();
        if (old != null) {
            // 잘못된 상황의 old를 덤프할것.
            throw new IllegalStateException("already Trace Object exist.");
        }
        // datasender연결 부분 수정 필요.
//        trace.setDataSender(this.dataSender);
        TimeLimitStorage storage = new TimeLimitStorage();
        storage.setDataSender(this.dataSender);
        trace.setStorage(storage);
        //
//        trace.setTransactionId(transactionId.getAndIncrement());
        threadLocal.set(trace);
    }

    public void detachTraceObject() {
        this.threadLocal.set(null);
    }

    public GlobalCallTrace getGlobalCallTrace() {
        return globalCallTrace;
    }

    public ActiveThreadCounter getActiveThreadCounter() {
        return activeThreadCounter;
    }

    public void setDataSender(DataSender dataSender) {
        this.dataSender = dataSender;
        this.globalCallTrace.setDataSender(dataSender);
    }
}
