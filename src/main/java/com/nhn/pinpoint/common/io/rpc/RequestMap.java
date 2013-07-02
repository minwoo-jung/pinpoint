package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.RequestPacket;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class RequestMap implements FailureHandle {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AtomicInteger requestId = new AtomicInteger(1);

    private final ConcurrentMap<Integer, MessageFuture> requestMap = new ConcurrentHashMap<Integer, MessageFuture>();
    // Timer를 factory로 옮겨야 되나?
    private final HashedWheelTimer timer;

    public RequestMap() {
        this(100);
    }

    public RequestMap(long timeoutTickDuration) {
        timer = new HashedWheelTimer(timeoutTickDuration, TimeUnit.MILLISECONDS);
        timer.start();
    }


    public MessageFuture registerRequest(final RequestPacket request, long timeoutMillis) {
        // shutdown check
        final int requestId = getNextRequestId();
        request.setRequestId(requestId);

        final MessageFuture future = new MessageFuture(requestId, timeoutMillis);

        final MessageFuture old = this.requestMap.put(requestId, future);
        if (old != null) {
            throw new SocketException("unexpected error. old future exist:" + old + " id:" + requestId);
        }
        // future가 실패하였을 경우 requestMap에서 빠르게 지울수 있도록 핸들을 넣는다.
        future.setFailureHandle(this);

        addTimeoutTask(timeoutMillis, future);
        return future;
    }

    private void addTimeoutTask(long timeoutMillis, MessageFuture future) {
        try {
            Timeout timeout = timer.newTimeout(future, timeoutMillis, TimeUnit.MILLISECONDS);
            future.setTimeout(timeout);
        } catch (IllegalStateException e) {
            // timer가 shutdown되었을 경우인데. 이것은 socket이 closed되었다는 의미뿐이 없을거임..
            future.setFailure(new SocketException("socket closed")) ;
        }
    }

    private int getNextRequestId() {
        return this.requestId.getAndIncrement();
    }

    public MessageFuture removeMessageFuture(int requestId) {
        return this.requestMap.remove(requestId);
    }

    public void close() {
        SocketException closed = new SocketException("connection closed");

        if (timer != null) {
            Set<Timeout> stop = timer.stop();
            for (Timeout timeout :stop) {
                MessageFuture future = (MessageFuture)timeout.getTask();
                future.setFailure(closed);
            }
        }

        for (Map.Entry<Integer, MessageFuture> entry : requestMap.entrySet()) {
            entry.getValue().setFailure(closed);
        }
        this.requestMap.clear();


    }

    @Override
    public void handleFailure(int requestId) {
        removeMessageFuture(requestId);
    }
}
