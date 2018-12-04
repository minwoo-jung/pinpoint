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
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.navercorp.test.pinpoint.testweb.repository.MongoDao;
import com.navercorp.test.pinpoint.testweb.repository.MongoDaoImpl;
import org.bson.Document;

import java.util.List;

public class MongoServiceImpl implements MongoService {

    private MongoDao dao;

    public MongoServiceImpl(MongoDatabase database, String collectionName) {
        dao = new MongoDaoImpl(database, collectionName);
    }

    public MongoServiceImpl(MongoDatabase database, ReadPreference readPreference, String collectionName) {
        dao = new MongoDaoImpl(database, readPreference, collectionName);
    }

    public MongoServiceImpl(MongoDatabase database, WriteConcern writeConcern, String collectionName) {
        dao = new MongoDaoImpl(database, writeConcern, collectionName);
    }

    public void add(Document doc) {
        dao.add(doc);
    }

    public DeleteResult delete(Document doc) {
        return dao.delete(doc);
    }

    public List<Document> list() {
        return dao.list();
    }

    public void find() {
        dao.find();
    }

    public void update(Document doc, Document todoc) {
        dao.update(doc, todoc);
    }

}
