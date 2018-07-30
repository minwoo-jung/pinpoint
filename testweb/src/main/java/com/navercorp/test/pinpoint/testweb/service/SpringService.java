/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.test.pinpoint.testweb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @author Woonduk Kang(emeroad)
 */
@Service
public class SpringService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void throwException() {
        throw new RuntimeException();
    }

    @Async
    public void asyncWithVoid() {
        logger.info("Asynchronous operation. thread={}" + Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    @Async
    public Future<String> asyncWithFuture() {
        logger.info("Asynchronous operation. thread={}" + Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        return new AsyncResult<String>("OK");
    }

    @Async("threadPoolTaskExecutor")
    public void asyncWithConfiguredExecutor() {
        logger.info("Asynchronous operation. thread={}" + Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }
}
