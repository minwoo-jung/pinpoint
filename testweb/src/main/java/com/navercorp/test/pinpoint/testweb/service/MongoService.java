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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

public interface MongoService<T> {

    void add(T doc);

    void addMany(List<T> doc);

    void update(Bson doc, Bson todoc);

    void update(Bson doc, Bson todoc, final UpdateOptions updateOptions);

    void updateWithClient(Bson doc, Bson todoc);

    DeleteResult delete(Bson doc);

    FindIterable<T> find(Bson doc);

    void addWithClient30(Document doc);

    void addWithDBWriteConcern(Document doc, WriteConcern unacknowledged);

    void addWithCollectionWriteConcern(Document doc, WriteConcern journaled);

    MongoCursor<Document> listWithDBReadPreference(ReadPreference readPreference);

    MongoCursor<Document> listWithCollectionReadPreference(ReadPreference readPreference);

    void addToSecondDB(Document doc);

    void close();

    void createIndex(Bson ascending);

    void createIndexes(List<IndexModel> indexes);
}
