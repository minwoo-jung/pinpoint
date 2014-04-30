package com.nhn.pinpoint.profiler.util;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.DummyInstrumentation;
import com.nhn.pinpoint.profiler.context.ReadableSpanStorageFactory;
import com.nhn.pinpoint.profiler.context.StorageFactory;
import com.nhn.pinpoint.profiler.sender.DataSender;
import com.nhn.pinpoint.profiler.sender.EnhancedDataSender;
import com.nhn.pinpoint.profiler.sender.LoggingDataSender;

import java.lang.instrument.Instrumentation;

/**
 * @author emeroad
 */
public class MockAgent extends DefaultAgent {

    public MockAgent(String agentArgs, ProfilerConfig profilerConfig) {
        this(agentArgs, new DummyInstrumentation(), profilerConfig);
    }

    public MockAgent(String agentArgs, Instrumentation instrumentation, ProfilerConfig profilerConfig) {
        super(agentArgs, instrumentation, profilerConfig);
    }

    @Override
    protected DataSender createUdpDataSender(int port, String threadName, int writeQueueSize) {
        return new LoggingDataSender();
    }

    @Override
    protected EnhancedDataSender createTcpDataSender() {
        return new LoggingDataSender();
    }
    
    @Override 
    protected StorageFactory createStorageFactory() {
		return new ReadableSpanStorageFactory();
    }
}
