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
 *
 */

package com.navercorp.test.pinpoint.testweb.service;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoServiceImpl implements MongoService<Document> {

    private final String databaseName = "myMongoDb";
    private final String databaseName2 = "myMongoDb2";
    private final String collectionName = "customers";

    @Autowired
    @Qualifier("mongoClientFactory")
    private com.mongodb.MongoClient mongoClient;

    @Autowired
    @Qualifier("mongoClientFactory37")
    private com.mongodb.client.MongoClient mongoClient_37;

    @Override
    public void add(Document doc) {
        mongoClient_37.getDatabase(databaseName).getCollection(collectionName).insertOne(doc);
    }

    @Override
    public void addMany(List<Document> doc) {
        mongoClient_37.getDatabase(databaseName).getCollection(collectionName).insertMany(doc);
    }

    @Override
    public DeleteResult delete(Bson doc) {
        return mongoClient_37.getDatabase(databaseName).getCollection(collectionName).deleteMany(doc);
    }

    @Override
    public void update(Bson doc, Bson todoc) {
        mongoClient_37.getDatabase(databaseName).getCollection(collectionName).updateOne(doc, todoc);
    }

    @Override
    public void update(Bson doc, Bson todoc, UpdateOptions updateOptions) {
        mongoClient_37.getDatabase(databaseName).getCollection(collectionName).updateOne(doc, todoc, updateOptions);
    }

    @Override
    public void updateWithClient(Bson doc, Bson todoc) {
        ClientSession session = mongoClient_37.startSession();
        mongoClient_37.getDatabase(databaseName).getCollection(collectionName).updateOne(session, doc, todoc);
    }

    @Override
    public FindIterable<Document> find(Bson doc) {
        return mongoClient_37.getDatabase(databaseName).getCollection(collectionName).find(doc);
    }

    @Override
    public void close() {
        mongoClient_37.close();
    }

    @Override
    public void createIndex(Bson ascending) {
        mongoClient_37.getDatabase(databaseName).getCollection(collectionName).createIndex(ascending);
    }

    @Override
    public void createIndexes(List<IndexModel> indexes) {
        mongoClient_37.getDatabase(databaseName).getCollection(collectionName).createIndexes(indexes);
    }

    public void addWithClient30(Document doc) {
        mongoClient.getDatabase(databaseName).getCollection(collectionName).insertOne(doc);
    }

    public void addToSecondDB(Document doc) {
        mongoClient_37.getDatabase(databaseName2).getCollection(collectionName).insertOne(doc);
    }

    public MongoCursor<Document> listWithDBReadPreference(ReadPreference readPreference) {
        return mongoClient_37.getDatabase(databaseName).withReadPreference(readPreference).getCollection(collectionName).find().iterator();
    }

    public MongoCursor<Document> listWithCollectionReadPreference(ReadPreference readPreference) {
        return mongoClient_37.getDatabase(databaseName).getCollection(collectionName).withReadPreference(readPreference).find().iterator();
    }

    public void addWithDBWriteConcern(Document doc, WriteConcern writeConcern) {
        mongoClient_37.getDatabase(databaseName).withWriteConcern(writeConcern).getCollection(collectionName).insertOne(doc);
    }

    public void addWithCollectionWriteConcern(Document doc, WriteConcern writeConcern) {
        mongoClient_37.getDatabase(databaseName).getCollection(collectionName).withWriteConcern(writeConcern).insertOne(doc);
    }

}
