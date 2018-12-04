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

package com.navercorp.test.pinpoint.testweb.repository;

import com.mongodb.Block;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;

import java.util.List;


public class MongoDaoImpl implements MongoDao {

    MongoCollection<Document> collection;

    public MongoDaoImpl(MongoDatabase database, String collectionName) {
        this.collection = database.getCollection(collectionName);
    }

    public MongoDaoImpl(MongoDatabase database, ReadPreference readPreference, String collectionName) {
        this.collection = database.getCollection(collectionName).withReadPreference(readPreference);
    }

    public MongoDaoImpl(MongoDatabase database, WriteConcern writeConcern, String collectionName) {
        this.collection = database.getCollection(collectionName).withWriteConcern(writeConcern);
    }


    public void add(Document doc) {

        collection.insertOne(doc);
    }

    public DeleteResult delete(Document doc) {
        return collection.deleteMany(doc);
    }

    public void find() {

        BsonDocument doc2 = new BsonDocument().append("name", new BsonString("roy2"))
                .append("company", new BsonString("Naver2"));

        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                System.out.println(document.toJson());
            }
        };

        collection.find(doc2).forEach(printBlock);
    }

    public List<Document> list() {
        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public void update(Document doc, Document todoc) {
        collection.updateOne(doc, new Document("$set", todoc));
    }
}
