package com.navercorp.test.pinpoint.testweb.service;

import org.springframework.stereotype.Service;

/**
 * @author jaehong.kim
 */
@Service
public interface VertxService {

    void request(int port, String host, String uri);


}
