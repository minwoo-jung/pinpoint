package com.profiler;

import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;
import com.profiler.util.Assert;


public class LifeCycleEventListener {

    private final static Logger logger = LoggerFactory.getLogger(LifeCycleEventListener.class.getName());

    private Agent agent;
    private boolean started = false;

    public LifeCycleEventListener(Agent agent) {
        Assert.notNull(agent, "agent must not be null");
        this.agent = agent;
    }

    public synchronized void start() {
        logger.info("LifeCycleEventListener start");

        if (started) {
            logger.info("already started");
            return;
        }

        agent.start();
        started = true;
    }

    public synchronized void stop() {
        logger.info("LifeCycleEventListener stop");

        if (!started) {
            logger.info("already stopped");
            return;
        }
        started = false;
        agent.stop();
    }
}
