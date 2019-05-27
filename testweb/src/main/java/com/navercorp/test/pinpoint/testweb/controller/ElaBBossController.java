/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.test.pinpoint.testweb.entity.DemoSearchResult;
import com.navercorp.test.pinpoint.testweb.service.ElaBBossServiceImpl;
import com.navercorp.test.pinpoint.testweb.util.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Roy Kim
 */
@Controller
public class ElaBBossController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ElaBBossServiceImpl elaBBossServie;

    @Description("/testBBossCrud")
    @RequestMapping(value = "/testBBossCrud")
    @ResponseBody
    public String testBBossCrud() {

        elaBBossServie.dropAndCreateAndGetIndice();
        elaBBossServie.addAndUpdateDocument();
        DemoSearchResult demoSearchResult = elaBBossServie.search();
        elaBBossServie.searchAllPararrel();
        elaBBossServie.deleteDocuments();

        return demoSearchResult.toString();
    }
}
