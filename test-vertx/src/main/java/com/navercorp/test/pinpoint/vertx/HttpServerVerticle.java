/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.test.pinpoint.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jaehong.kim
 */
public class HttpServerVerticle extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private HttpServer httpServer;

    @Override
    public void start() throws Exception {
        HttpServerOptions options = new HttpServerOptions();
        options.setIdleTimeout(1000);

        Router router = Router.router(vertx);
        router.get("/").handler(routingContext -> {
            routingContext.response().end(usage(router));
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


        this.httpServer = vertx.createHttpServer().requestHandler(router::accept).listen(HttpServerConfig.getPort(), result -> {
            if (result.succeeded()) {
                logger.info("Started server. port={}", result.result().actualPort());
            } else {
                logger.info("Failed to start server.", result.cause());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        if (this.httpServer != null) {
            this.httpServer.close();
        }
    }

    private String usage(final Router router) {
        StringBuilder sb = new StringBuilder();
        for(Route route : router.getRoutes()) {
            sb.append("<a href=\"").append(route.getPath()).append("\">").append(route.getPath()).append("</a><br>");
        }
        return sb.toString();
    }

    public void request(int port, String host, String uri) {
        final HttpClient client = vertx.createHttpClient();
        client.request(HttpMethod.GET, port, host, uri, new Handler<HttpClientResponse>() {
            @Override
            public void handle(HttpClientResponse httpClientResponse) {
                httpClientResponse.exceptionHandler(new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable throwable) {
                        logger.info("Failed to request", throwable);
                    }
                });
                httpClientResponse.endHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void aVoid) {
                        logger.info("End request.");
                    }
                });
            }
        }).end();
    }

    private void sleep(int waiteSeconds) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(waiteSeconds));
        } catch (InterruptedException e) {
        }
    }

    // for debug
    private final AtomicInteger counter = new AtomicInteger();

    private void executeBlocking(HttpServerRequest request, final int waitSeconds) {
        int debugCount = counter.incrementAndGet();
        logger.info("executeBlocking: id{} {}", debugCount, waitSeconds);

        vertx.executeBlocking(new Handler<Future<Object>>() {
            @Override
            public void handle(Future<Object> objectFuture) {
                logger.info("sleep:{}", waitSeconds);
                sleep(waitSeconds);
                request.response().end("Execute blocking." + waitSeconds);
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
