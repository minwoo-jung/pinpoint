package com.navercorp.test.pinpoint.testweb.service;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.impl.VertxHandler;
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
        createHttpServer();
    }

    @PreDestroy
    public void destroy() {
        if (vertx != null) {
            vertx.close();
        }
    }

    @Override
    public void request(int port, String host, String uri) {
        HttpClient client = vertx.createHttpClient();
        client.get(port, host, uri).handler(new Handler<HttpClientResponse>() {
            @Override
            public void handle(HttpClientResponse httpClientResponse) {
                logger.debug("Response {} {}", httpClientResponse.statusCode(), httpClientResponse.statusMessage());
                httpClientResponse.exceptionHandler(new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable throwable) {
                        logger.debug("Response exception", throwable);
                    }
                });
                httpClientResponse.endHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void aVoid) {
                        logger.debug("Response end.");
                    }
                });
            }
        }).end();
    }

    private void createHttpServer() {
        HttpServerOptions options = new HttpServerOptions();
        options.setIdleTimeout(1000);
        vertx.createHttpServer().requestHandler(r -> {
            if (r.uri().equals("/")) {
                r.response().end("Welcome pinpoint vert.x HTTP server test.");
            } else if (r.uri().equals("/request")) {
                request(80, "naver.com", "/");
                r.response().end("Request.");
            } else if (r.uri().equals("/noresponse")) {
            } else if (r.uri().equals("/close")) {
                r.response().close();
            } else if (r.uri().equals("/connection/close")) {
                r.connection().close();
            } else if (r.uri().equals("/executeBlocking")) {
                executeBlocking(r);
            } else if (r.uri().equals("/executeBlocking/request")) {
                executeBlockingRequest(r);
            } else if (r.uri().equals("/runOnContext")) {
                runOnContext(r);
            } else if (r.uri().equals("/runOnContext/request")) {
                runOnContextRequest(r);
            } else {
                r.response().end("Unknown request. " + r.uri());
            }
        }).listen(VertxService.LISTEN_PORT, result -> {
            if (result.succeeded()) {
                logger.info("Started HTTP server.");
            } else {
                logger.info("Failed HTTP server.");
            }
        });
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    private void executeBlocking(HttpServerRequest request) {
        vertx.executeBlocking(new Handler<Future<Object>>() {
            @Override
            public void handle(Future<Object> objectFuture) {
                sleep();
                request.response().end("Execute blocking.");
            }
        }, false, null);
    }

    private void executeBlockingRequest(HttpServerRequest request) {
        vertx.executeBlocking(new Handler<Future<Object>>() {
            @Override
            public void handle(Future<Object> objectFuture) {
                request(80, "naver.com", "/");
                request.response().end("Execute blocking request.");
            }
        }, false, null);
    }

    private void runOnContext(HttpServerRequest request) {
        vertx.runOnContext(aVoid -> {
            sleep();
            request.response().end("Run on context");
        });

    }

    private void runOnContextRequest(HttpServerRequest request) {
        vertx.runOnContext(aVoid -> {
            request(80, "naver.com", "/");
            request.response().end("Run on context request.");
        });
    }
}