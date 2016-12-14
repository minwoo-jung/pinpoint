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

package com.navercorp.test.pinpoint.testweb.controller;

import com.navercorp.test.pinpoint.testweb.service.NpcService;
import com.nhncorp.lucy.npc.connector.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.InetSocketAddress;

/**
 * @author netspider
 */
@Controller
public class NPCController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final InetSocketAddress bloc3ServerAddress = new InetSocketAddress("10.110.241.190", 5000);
    private final InetSocketAddress bloc4ServerAddress = new InetSocketAddress("10.110.241.190", 15000);

    @Autowired
    private NpcService npcService;

    /**
     * using basic connector
     *
     * @return
     */
    @RequestMapping(value = "/npc/invokeAndReturn/target/bloc3")
    @ResponseBody
    public String invokeAndReturnToBloc3() throws Exception {
        npcService.invoke(bloc3ServerAddress);
        return "OK";
    }

    @RequestMapping(value = "/npc/invokeAndReturn/target/bloc4")
    @ResponseBody
    public String invokeAndReturnToBloc4() throws Exception {
        npcService.invoke(bloc4ServerAddress);
        return "OK";
    }


    /**
     * using keepalive connector
     *
     * @return
     */
    @RequestMapping(value = "/npc/keepalive/target/bloc3")
    @ResponseBody
    public String keepaliveToBloc3() throws Exception {
        npcService.keepalive(bloc3ServerAddress);
        return "OK";
    }

    @RequestMapping(value = "/npc/keepalive/target/bloc4")
    @ResponseBody
    public String keepaliveToBloc4() throws Exception {
        npcService.keepalive(bloc4ServerAddress);
        return "OK";
    }

    /**
     * using connection factory
     *
     * @return
     */
    @RequestMapping(value = "/npc/factory/target/bloc3")
    @ResponseBody
    public String factoryToBloc3() throws Exception {
        npcService.factory(bloc3ServerAddress);
        return "OK";
    }

    @RequestMapping(value = "/npc/factory/target/bloc4")
    @ResponseBody
    public String factoryToBloc4() throws Exception {
        npcService.factory(bloc4ServerAddress);
        return "OK";
    }

    /**
     * using lightweight connector
     *
     * @return
     */
    @RequestMapping(value = "/npc/lightweight/target/bloc3")
    @ResponseBody
    public String lightweightToBloc3() throws Exception {
        npcService.lightweight(bloc3ServerAddress);
        return "OK";
    }

    @RequestMapping(value = "/npc/lightweight/target/bloc4")
    @ResponseBody
    public String lightweightToBloc4() throws Exception {
        npcService.lightweight(bloc4ServerAddress);
        return "OK";
    }

    /**
     * using lightweight connector and listener
     *
     * @return
     */
    @RequestMapping(value = "/npc/listener/target/bloc3")
    @ResponseBody
    public String listenerToBloc3() throws NpcCallException {
        Runnable callback = new Runnable() {
            public void run() {
                logger.info("Completed npc listen");
            }
        };

        npcService.listener(bloc3ServerAddress, callback);
        return "OK";
    }

    @RequestMapping(value = "/npc/listener/target/bloc4")
    @ResponseBody
    public String listenerToBloc4() throws NpcCallException {
        Runnable callback = new Runnable() {
            public void run() {
                logger.info("Completed npc listen");
            }
        };

        npcService.listener(bloc4ServerAddress, callback);
        return "OK";
    }
}