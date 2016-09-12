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

import java.util.HashMap;

import com.navercorp.test.pinpoint.testweb.connector.apachehttp4.ApacheHttpClient4;
import org.springframework.stereotype.Service;

import com.navercorp.test.pinpoint.testweb.connector.apachehttp4.HttpConnectorOptions;

@Service
public class DummyService {

    /**
     * <pre>
     * doSomething();        // 2
     *         a();            // 3
     *         b();            // 3
     *             ba();        // 4
     *                 baa();    // 5
     *             bb();        // 4
     *         c();            // 3
     *             ca();        // 4
     * </pre>
     */
    public void doSomething() {
        System.out.println("do something.");
        a();
        b();
        c();
    }

    private void a() {
        System.out.println("a");
    }

    private void b() {
        System.out.println("b");
        ba();
        bb();
    }

    private void ba() {
        System.out.println("ba");
        baa();
    }

    private void bb() {
        System.out.println("bb");
    }

    private void baa() {
        System.out.println("baa");
    }

    private void c() {
        System.out.println("c");
        ca();
    }

    private void ca() {
        System.out.println("ca");
        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        client.execute("http://localhost:8080/mysqlsimple.pinpoint", new HashMap<String, Object>());
    }
}
