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

package com.navercorp.test.pinpoint.testweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Taejin Koo
 */
@Controller
public class ThreadDeadlockController {

    private Object lock1 = new Object();
    private Object lock2 = new Object();

    @RequestMapping("/deadlock")
    @ResponseBody
    public String deadlock() {

        try {
            Thread.sleep(10000);

            Thread thread = new Thread1("DEADLOCK THREAD-1", 10000L, lock1, lock2);
            thread.start();

            thread = new Thread1("DEADLOCK THREAD-2", 10000L, lock2, lock1);
            thread.start();

            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "ok";
    }

    class Thread1 extends Thread {

        private final String name;
        private final long waitTime;
        private final Object lock1;
        private final Object lock2;

        public Thread1(String name, long waitTime, Object lock1, Object lock2) {
            super();
            this.name = name;
            this.waitTime = waitTime;
            this.lock1 = lock1;
            this.lock2 = lock2;
            setName(name);
        }

        @Override
        public void run() {
            synchronized (lock1) {
                long startTime = System.currentTimeMillis();

                while (true) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime > waitTime) {
                        break;
                    }
                }

                synchronized (lock2) {
                    while (true) {
                    }
                }
            }
        }
    }

}
