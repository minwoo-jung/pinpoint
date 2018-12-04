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

package com.navercorp.test.pinpoint.testweb.controller;

import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.navercorp.test.pinpoint.testweb.service.MongoService;
import com.navercorp.test.pinpoint.testweb.service.MongoServiceImpl;
import com.navercorp.test.pinpoint.testweb.util.Description;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.BsonBoolean;
import org.bson.BsonDateTime;
import org.bson.BsonDbPointer;
import org.bson.BsonDecimal128;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonJavaScript;
import org.bson.BsonJavaScriptWithScope;
import org.bson.BsonNull;
import org.bson.BsonObjectId;
import org.bson.BsonRegularExpression;
import org.bson.BsonString;
import org.bson.BsonSymbol;
import org.bson.BsonTimestamp;
import org.bson.BsonUndefined;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.Date;

/**
 *
 */

@Controller
public class MongoDBController {

    private static final String IP = "10.106.157.82";

    private MongoClient mongoClient;
    private com.mongodb.MongoClient mongoClient_36;
    private MongoDatabase database;
    private MongoDatabase database2;
    private MongoService mongoService;
    private MongoService mongoService2;

    public MongoDBController() {

    }

    @Description("connect to myMongoDb, myMongoDb2")
    @RequestMapping(value = "/mongodb/connect")
    @ResponseBody
    public String connect() {

        mongoClient = MongoClients.create(
                MongoClientSettings.builder().readPreference(ReadPreference.secondary())
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress(IP, 27017)))).build());

        database = mongoClient.getDatabase("myMongoDb");
        database2 = mongoClient.getDatabase("myMongoDb2");

        mongoService = new MongoServiceImpl(database, "customers");
        mongoService2 = new MongoServiceImpl(database2, "customers");

        return "OK";
    }

    @Description("Mongo_JavaDriver3.6 이하 방식으로 connect")
    @RequestMapping(value = "/mongodb/connectWith_v3.6")
    @ResponseBody
    public String connect36() {

        mongoClient_36 = new com.mongodb.MongoClient(IP, 27017);

        database = mongoClient_36.getDatabase("myMongoDb36");
        mongoService = new MongoServiceImpl(database, "customers");

        return "OK";
    }

    @Description("connect to myMongoDb, myMongoDb2 withDBReadPreference")
    @RequestMapping(value = "/mongodb/connectWithDBReadPreference")
    @ResponseBody
    public String connect2() {

        mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress(IP, 27017)))).build());

        database = mongoClient.getDatabase("myMongoDb").withReadPreference(ReadPreference.primary());
        database2 = mongoClient.getDatabase("myMongoDb2").withReadPreference(ReadPreference.secondaryPreferred());

        mongoService = new MongoServiceImpl(database, "customers");
        mongoService2 = new MongoServiceImpl(database2, "customers");

        return "OK";
    }

    @Description("set collection in myMongoDb with primaryPreferred ")
    @RequestMapping(value = "/mongodb/connectWithCollectionReadPreference")
    @ResponseBody
    public String connect3() {

        database = mongoClient.getDatabase("myMongoDb");
        mongoService = new MongoServiceImpl(database, ReadPreference.primaryPreferred(), "customers");

        return "OK";
    }

    @Description("check multiple collections with writeConcern")
    @RequestMapping(value = "/mongodb/connectWithCollectionChange")
    @ResponseBody
    public String connect4() {


        MongoService mongoService = new MongoServiceImpl(database, "collection1");

        Document doc = new Document("name", "pinpoint").append("company", "Naver");
        mongoService.add(doc);

        MongoService mongoService2 = new MongoServiceImpl(database, WriteConcern.UNACKNOWLEDGED, "collection2");

        Document doc2 = new Document("name", "pinpoint2").append("company", "Naver2");
        mongoService2.add(doc2);

        MongoService mongoService3 = new MongoServiceImpl(database2, "collection3");
        Document doc3 = new Document("name", "pinpoint3").append("company", "Naver3");
        mongoService3.add(doc3);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/insert1")
    @ResponseBody
    public String insert1() {

        Document doc = new Document("name", "pinpoint").append("company", "Naver");
        mongoService.add(doc);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/insertComplexBson")
    @ResponseBody
    public String insertComplex() {

        BsonInt32[] bsonInt32s = new BsonInt32[40];
        for (int i = 0; i < 40; i++) {
            bsonInt32s[i] = new BsonInt32(i + 1);
        }

        BsonValue a = new BsonString("stest");
        BsonValue b = new BsonDouble(111);
        BsonValue c = new BsonBoolean(true);

        Document doc = new Document()
                .append("int32", new BsonInt32(12))
                .append("int64", new BsonInt64(77L))
                .append("bo\"olean", new BsonBoolean(true))
                .append("date", new BsonDateTime(new Date().getTime()))
                .append("double", new BsonDouble(12.3))
                .append("string", new BsonString("pinpoint"))
                .append("objectId", new BsonObjectId(new ObjectId()))
                .append("code", new BsonJavaScript("int i = 10;"))
                .append("codeWithScope", new BsonJavaScriptWithScope("int x = y", new BsonDocument("y", new BsonInt32(1))))
                .append("regex", new BsonRegularExpression("^test.*regex.*xyz$", "big"))
                .append("symbol", new BsonSymbol("wow"))
                .append("timestamp", new BsonTimestamp(0x12345678, 5))
                .append("undefined", new BsonUndefined())
                .append("binary1", new BsonBinary(new byte[]{(byte) 0xe0, 0x4f, (byte) 0xd0, 0x20}))
                .append("oldBinary", new BsonBinary(BsonBinarySubType.OLD_BINARY, new byte[]{1, 1, 1, 1, 1}))
                .append("arrayInt", new BsonArray(Arrays.asList(a, b, c, new BsonInt32(7))))
                .append("abbreviation", new BsonArray(Arrays.asList(bsonInt32s)))
                .append("document", new BsonDocument("a", new BsonInt32(77)))
                .append("dbPointer", new BsonDbPointer("db.coll", new ObjectId()))
                .append("null", new BsonNull())
                .append("decimal128", new BsonDecimal128(new Decimal128(55)));
        mongoService.add(doc);

        return "OK";
    }

    @Description("2번째 DB 인 myMongoDb2")
    @RequestMapping(value = "/mongodb/insertTo2ndserver")
    @ResponseBody
    public String insert1to2ndserver() {

        Document doc = new Document("name", "pinpoint").append("company", "Naver");
        mongoService2.add(doc);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/insertNested")
    @ResponseBody
    public String insert2() {

        Document doc2 = new Document("name", "pinpoint2").append("company", new Document("nestedDoc", "1"));
        mongoService.add(doc2);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/insertArray")
    @ResponseBody
    public String insert3() {

        BsonValue a = new BsonDouble(111);
        BsonValue b = new BsonDouble(222);
        BsonArray bsonArray = new BsonArray();
        bsonArray.add(a);
        bsonArray.add(b);

        Document doc2 = new Document("array", bsonArray);
        mongoService.add(doc2);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/insertCollection")
    @ResponseBody
    public String insert4() {

        Document doc2 = new Document("Java_Collection", Arrays.asList("naver", "apple"));
        mongoService.add(doc2);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/find")
    @ResponseBody
    public String find() {

        mongoService.find();
        return "OK";
    }


    @RequestMapping(value = "/mongodb/list")
    @ResponseBody
    public String list() {
        mongoService.list();
        return "OK";
    }


    @RequestMapping(value = "/mongodb/update")
    @ResponseBody
    public String update2to3() {

        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");
        Document doc2 = new Document("name", "pinpoint2").append("company", "Naver2");

        mongoService.update(doc1, doc2);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/delete")
    @ResponseBody
    public String delete() {

        Document doc = new Document("name", "pinpoint").append("company", "Naver");
        DeleteResult deleteResult = mongoService.delete(doc);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/close")
    @ResponseBody
    public String close() {

        mongoClient.close();

        return "OK";
    }
}
