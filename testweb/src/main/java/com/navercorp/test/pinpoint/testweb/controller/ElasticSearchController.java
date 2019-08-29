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

import com.navercorp.test.pinpoint.testweb.service.ElasticSearchServiceImpl;
import com.navercorp.test.pinpoint.testweb.util.Description;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

/**
 * @author Roy Kim
 */
@Controller
public class ElasticSearchController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ElasticSearchServiceImpl elasticSearchService;

    @Description("/testElasticIndex")
    @RequestMapping(value = "/testElasticIndex")
    @ResponseBody
    public String testIndex() throws IOException {

        IndexResponse response = elasticSearchService.index();

        return response.toString();
    }

    @Description("/testElasticGet")
    @RequestMapping(value = "/testElasticGet")
    @ResponseBody
    public String testGet() throws IOException {

        GetResponse response = elasticSearchService.get();

        return response.toString();
    }

    @Description("/testElasticUpdate")
    @RequestMapping(value = "/testElasticUpdate")
    @ResponseBody
    public String testUpdate() throws IOException {

        UpdateResponse response = elasticSearchService.update();

        return response.toString();
    }

    @Description("/testElasticUpsert")
    @RequestMapping(value = "/testElasticUpsert")
    @ResponseBody
    public String testUpsert() throws IOException {

        UpdateResponse response = elasticSearchService.upsert();

        return response.toString();
    }

    @Description("/testElasticSearch")
    @RequestMapping(value = "/testElasticSearch")
    @ResponseBody
    public String testSearch() throws IOException {

        SearchResponse response = elasticSearchService.search();

        return response.toString();
    }

    @Description("/testElasticDelete")
    @RequestMapping(value = "/testElasticDelete")
    @ResponseBody
    public String testDelete() throws IOException {

        DeleteResponse response = elasticSearchService.delete();

        return response.toString();
    }

    @Description("/testElasticAsyncIndex")
    @RequestMapping(value = "/testElasticAsyncIndex")
    @ResponseBody
    public String testAsyncIndex() throws IOException {

        IndexResponse response = elasticSearchService.indexAsync();

        if(response == null) {
            logger.debug("it's null");
        }else{
            logger.debug(response.toString());
        }

        return "testAsyncIndex process finished";
    }

    @Description("/testElasticAsyncGet")
    @RequestMapping(value = "/testElasticAsyncGet")
    @ResponseBody
    public String testAsyncGet() throws IOException {

        GetResponse response = elasticSearchService.getAsync();

        if(response == null) {
            logger.debug("it's null");
        }else{
            logger.debug(response.toString());
        }

        return "testAsyncGet process finished";
    }

}
