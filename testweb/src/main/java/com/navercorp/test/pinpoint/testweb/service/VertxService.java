package com.navercorp.test.pinpoint.testweb.service;

import org.springframework.stereotype.Service;

/**
 * @author jaehong.kim
 */
@Service
public interface VertxService {
    public static final int LISTEN_PORT = 40010;

    void request(int port, String host, String uri);
}
