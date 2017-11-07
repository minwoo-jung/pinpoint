package com.navercorp.test.pinpoint.testweb.service;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

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
    public void sendHead(int port, String host, String uri) {
        final HttpClient client = vertx.createHttpClient();
        client.request(HttpMethod.GET, port, host, uri, new Handler<HttpClientResponse>() {
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
        }).sendHead(new Handler<HttpVersion>() {
            @Override
            public void handle(HttpVersion httpVersion) {
                logger.debug("Send head. httpVersion={}", httpVersion);
            }
        });
    }

    @Override
    public void request(int port, String host, String uri) {
        final long currentTimeMillis = System.currentTimeMillis();
        final HttpClient client = vertx.createHttpClient();

        client.request(HttpMethod.GET, port, host, uri, new Handler<HttpClientResponse>() {
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
        }).putHeader("Pinpoint-ProxyApp", "t=" + currentTimeMillis + " app=foo-bar").end();
    }

    @Override
    public void request(int port, String host, String uri, String body) {
        final HttpClient client = vertx.createHttpClient();
        client.request(HttpMethod.POST, port, host, uri, new Handler<HttpClientResponse>() {
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
        }).putHeader("content-length", String.valueOf(body.length())).write(body).end();
    }

    @Override
    public void requestAbs(String url) {
        final HttpClient client = vertx.createHttpClient();
        client.requestAbs(HttpMethod.GET, url).handler(new Handler<HttpClientResponse>() {
            @Override
            public void handle(HttpClientResponse httpClientResponse) {
                logger.debug("Response {} {}", httpClientResponse.statusCode(), httpClientResponse.statusMessage());
            }
        }).end();
    }

    public void chunk(int port, String host, String uri, String body) {
        final HttpClient client = vertx.createHttpClient();
        client.request(HttpMethod.POST, port, host, uri, new Handler<HttpClientResponse>() {
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
        }).end(body);
    }

    private void createHttpServer() {
        HttpServerOptions options = new HttpServerOptions();
        options.setIdleTimeout(1000);
        Router router = Router.router(vertx);

        router.get("/").handler(routingContext -> {
            routingContext.response().end("Welcome pinpoint vert.x HTTP server test.");
        });
        router.get("/request").handler(routingContext -> {
            request(80, "naver.com", "/");
            routingContext.response().end("Request http://naver.com:80/");
        });
        router.get("/noresponse").handler(routingContext -> {
        });
        router.get("/close").handler(routingContext -> {
            routingContext.response().close();
        });
        router.get("/connection/close").handler(routingContext -> {
            routingContext.request().connection().close();
        });
        router.get("/executeBlocking").handler(routingContext -> {
            executeBlocking(routingContext.request(), 1);
        });
        router.get("/executeBlocking/wait10s").handler(routingContext -> {
            executeBlocking(routingContext.request(), 10);
        });
        router.get("/executeBlocking/request").handler(routingContext -> {
            executeBlockingRequest(routingContext.request());
        });
        router.get("/runOnContext").handler(routingContext -> {
            runOnContext(routingContext.request(), 1);
        });
        router.get("/runOnContext/wait10s").handler(routingContext -> {
            runOnContext(routingContext.request(), 10);
        });
        router.get("/runOnContext/request").handler(routingContext -> {
            runOnContextRequest(routingContext.request());
        });

        vertx.createHttpServer().requestHandler(router::accept).listen(VertxService.LISTEN_PORT, result -> {
            if (result.succeeded()) {
                logger.info("Started HTTP server.");
            } else {
                logger.info("Failed HTTP server.");
            }
        });
    }

    private void sleep(int waiteSeconds) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(waiteSeconds));
        } catch (InterruptedException e) {
        }
    }

    private void executeBlocking(HttpServerRequest request, final int waitSeconds) {
        vertx.executeBlocking(new Handler<Future<Object>>() {
            @Override
            public void handle(Future<Object> objectFuture) {
                sleep(waitSeconds);
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

    private void runOnContext(HttpServerRequest request, final int waitSeconds) {
        vertx.runOnContext(aVoid -> {
            sleep(waitSeconds);
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