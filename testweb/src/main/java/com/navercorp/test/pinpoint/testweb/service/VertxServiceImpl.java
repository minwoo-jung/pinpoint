package com.navercorp.test.pinpoint.testweb.service;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author jaehong.kim
 */
@Service("vertxService")
public class VertxServiceImpl implements VertxService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Vertx vertx;

    @PostConstruct
    public void init() throws Exception {
        vertx = Vertx.vertx(new VertxOptions());
    }

    @PreDestroy
    public void destroy() {
        if (vertx != null) {
            vertx.close();
        }
    }


    @Override
    public void request(int port, String host, String uri)  {
        HttpClient client = vertx.createHttpClient();
        client.get(port, host, uri).handler(new Handler<HttpClientResponse>() {
            @Override
            public void handle(HttpClientResponse httpClientResponse) {
                logger.debug("Response {} {}", httpClientResponse.statusCode(), httpClientResponse.statusMessage());
                httpClientResponse.handler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buffer) {
                        logger.debug("Response {}", buffer.toString());
                    }
                });
            }
        }).end();
    }
}
