/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.test.pinpoint.testweb.connector.grpc.HelloWorldStreamClient;
import com.navercorp.test.pinpoint.testweb.connector.grpc.HelloWorldStreamServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Taejin Koo
 */
@Controller
public class GrpcStreamController {

    @Autowired
    private HelloWorldStreamServer helloWorldStreamServer;

    @RequestMapping(value = "/grpc/stream/client/call")
    @ResponseBody
    public String call() throws InterruptedException {
        HelloWorldStreamClient helloWorldStreamClient = new HelloWorldStreamClient("127.0.0.1", helloWorldStreamServer.getBindPort());
        try {
            helloWorldStreamClient.greet(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "ok";
    }

}
