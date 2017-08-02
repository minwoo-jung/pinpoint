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

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author jaehong.kim
 */
public class Server {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Vertx vertx;

    public static void main(String[] args) {
        System.out.println("Starting server. args is " + Arrays.asList(args));
        Server server = new Server();
        if (args.length == 1) {
            try {
                int port = Integer.parseInt(args[0]);
                HttpServerConfig.setPort(port);
            } catch (NumberFormatException ignored) {
            }
        }

        server.startServer();
    }


    public void startServer() {
        logger.info("Start server.");
        this.vertx = Vertx.vertx();

        DeploymentOptions options = new DeploymentOptions();
        options.setInstances(1);

        this.vertx.deployVerticle(HttpServerVerticle.class.getName(), options);
        logger.info("Deploy verticle.");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopServer();
            }
        });
    }

    private void stopServer() {
        logger.info("Stop server.");
        if (vertx != null) {
            final CountDownLatch latch = new CountDownLatch(1);
            vertx.close(voidAsyncResult -> {
                latch.countDown();
            });

            try {
                latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        }
    }

}
