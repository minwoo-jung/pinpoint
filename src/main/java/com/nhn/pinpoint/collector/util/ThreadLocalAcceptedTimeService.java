package com.nhn.pinpoint.collector.util;

import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class ThreadLocalAcceptedTimeService implements AcceptedTimeService {

    private final ThreadLocal<Long> local = new NamedThreadLocal<Long>("AcceptedTimeService");

    @Override
    public void accept() {
        local.set(System.currentTimeMillis());
    }

    @Override
    public long getAcceptedTime() {
        Long acceptedTime = local.get();
        if (acceptedTime == null) {
            return System.currentTimeMillis();
        }
        return acceptedTime;
    }
}
