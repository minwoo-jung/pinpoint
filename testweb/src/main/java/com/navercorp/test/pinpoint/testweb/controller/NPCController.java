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

/**
 * @author netspider
 */
@Controller
public class NPCController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NpcService npcService;

    /**
     * using basic connector
     *
     * @return
     */
    @RequestMapping(value = "/npc/invokeAndReturn")
    @ResponseBody
    public String invokeAndReturn() throws Exception {
        npcService.invoke();
        return "OK";
    }

    /**
     * using keepalive connector
     *
     * @return
     */
    @RequestMapping(value = "/npc/keepalive")
    @ResponseBody
    public String keepalive() throws Exception {
        npcService.keepalive();
        return "OK";
    }

    /**
     * using connection factory
     *
     * @return
     */
    @RequestMapping(value = "/npc/factory")
    @ResponseBody
    public String factory() throws Exception {
        npcService.factory();
        return "OK";
    }

    /**
     * using lightweight connector
     *
     * @return
     */
    @RequestMapping(value = "/npc/lightweight")
    @ResponseBody
    public String lightweight() throws Exception {
        npcService.lightweight();
        return "OK";
    }

    /**
     * using lightweight connector and listener
     *
     * @return
     */
    @RequestMapping(value = "/npc/listener")
    @ResponseBody
    public String listener() throws NpcCallException {
        Runnable callback = new Runnable() {
            public void run() {
                logger.info("Completed npc listen");
            }
        };

        npcService.listener(callback);
        return "OK";
    }
}