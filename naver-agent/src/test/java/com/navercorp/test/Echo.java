package com.navercorp.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Echo {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public String echo(String message) {
        logger.info(message);
        return message;
    }
    
    public void empty() {
    }
}
