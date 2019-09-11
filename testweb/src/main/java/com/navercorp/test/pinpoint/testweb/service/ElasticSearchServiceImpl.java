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

package com.navercorp.test.pinpoint.testweb.service;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author Roy Kim
 */
@Service
public class ElasticSearchServiceImpl {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("restHighLevelClientFactory")
    private RestHighLevelClient restHighLevelClient;

    public IndexResponse index() throws IOException {

        IndexRequest request = new IndexRequest(
                "post2");
        request.id("1");

        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        request.source(jsonString, XContentType.JSON);

        return restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    public GetResponse get() throws IOException {

        GetRequest request = new GetRequest(
                "post2", "1");

        return restHighLevelClient.get(request, RequestOptions.DEFAULT);
    }

    public UpdateResponse update() throws IOException {
        UpdateRequest request = new UpdateRequest("post2", "1");
        String jsonString = "{" +
                "\"updated\":\"2017-01-02\"," +
                "\"reason\":\"daily update\"" +
                "}";
        request.doc(jsonString, XContentType.JSON);
        request.upsert(jsonString, XContentType.JSON);

        restHighLevelClient.update(request, RequestOptions.DEFAULT);

        return restHighLevelClient.update(request, RequestOptions.DEFAULT);
    }

    public UpdateResponse upsert() throws IOException {
        UpdateRequest request = new UpdateRequest("post3", "3");
        String jsonString = "{" +
                "\"updated\":\"2017-01-02\"," +
                "\"reason\":\"daily update\"" +
                "}";
        request.doc(jsonString, XContentType.JSON);
        request.upsert(jsonString, XContentType.JSON);

        return restHighLevelClient.update(request, RequestOptions.DEFAULT);
    }

    public DeleteResponse delete() throws IOException {

        DeleteRequest request = new DeleteRequest(
                "post2","1");

        return restHighLevelClient.delete(request, RequestOptions.DEFAULT);
    }


    public SearchResponse search() throws IOException {

        SearchRequest request = new SearchRequest("post2");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        request.source(searchSourceBuilder);

        return restHighLevelClient.search(request, RequestOptions.DEFAULT);

    }

    public IndexResponse indexAsync() {

        IndexRequest request = new IndexRequest(
                "post2");
        request.id("4");

        String jsonString = "{" +
                "\"user\":\"async kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        request.source(jsonString, XContentType.JSON);

        Transporter transporter = new Transporter();

        restHighLevelClient.indexAsync(request, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                logger.debug("indexAsync response Success");
                logger.debug(indexResponse.status().toString());
                transporter.setObject(indexResponse);
            }

            @Override
            public void onFailure(Exception e) {
                logger.debug("Index Async Failed");
            }
        });

        return (IndexResponse)transporter.getObject();
    }

    public GetResponse getAsync() {

        GetRequest request = new GetRequest(
                "post2", "1");

        Transporter transporter = new Transporter();

        restHighLevelClient.getAsync(request, RequestOptions.DEFAULT, new ActionListener<GetResponse>() {
            @Override
            public void onResponse(GetResponse getResponse) {
                logger.debug("getAsync response Success");
                logger.debug(getResponse.getId());
                transporter.setObject(getResponse);
            }

            @Override
            public void onFailure(Exception e) {
                logger.debug("Get Async Failed");
            }
        });

        return (GetResponse)transporter.getObject();
    }

    public GetResponse getV6() throws IOException {

        GetRequest request = new GetRequest(
                "postsv6",
                "doc",
                "1");

        return restHighLevelClient.get(request, RequestOptions.DEFAULT);
    }

    public IndexResponse indexV6() throws IOException {

        IndexRequest request = new IndexRequest(
                "postsv6",
                "doc",
                "1");

        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        request.source(jsonString, XContentType.JSON);

        return restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    class Transporter {

        Object object;
        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

    }
}
