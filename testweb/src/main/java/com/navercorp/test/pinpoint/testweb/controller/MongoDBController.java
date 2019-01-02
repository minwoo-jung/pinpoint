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
 *
 */

package com.navercorp.test.pinpoint.testweb.controller;

import com.mongodb.Block;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.PushOptions;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.navercorp.test.pinpoint.testweb.service.MongoService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static java.util.Arrays.asList;

/**
 *
 */

@Controller
public class MongoDBController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MongoService<Document> mongoService;


    @Description("withDBReadPreference test")
    @RequestMapping(value = "/mongodb/connectWithReadPreference")
    @ResponseBody
    public String readPreference() {

        MongoCursor<Document> cursor2 = mongoService.listWithDBReadPreference(ReadPreference.primaryPreferred());

        MongoCursor<Document> cursor = mongoService.listWithCollectionReadPreference(ReadPreference.secondaryPreferred());
        try {
            while (cursor.hasNext()) {
                logger.info(cursor.next().toJson());
            }
        } finally {
            cursor.close();
        }

        return "OK";
    }

    @Description("writeConcern test")
    @RequestMapping(value = "/mongodb/connectWithWriteConcern")
    @ResponseBody
    public String writeConcern() {

        Document doc = new Document("name", "pinpoint").append("company", "Naver");
        mongoService.addWithDBWriteConcern(doc, WriteConcern.UNACKNOWLEDGED);

        Document doc2 = new Document("name", "pinpoint2").append("company", "Naver2");
        mongoService.addWithCollectionWriteConcern(doc2, WriteConcern.JOURNALED);

        Document doc3 = new Document("name", "pinpoint3").append("company", "Naver3");
        mongoService.addWithDBWriteConcern(doc3, WriteConcern.MAJORITY);


        return "OK";
    }

    @RequestMapping(value = "/mongodb/insert")
    @ResponseBody
    public String insert() {
        Document doc = new Document("name", "pinpoint").append("company", "Naver");
        mongoService.add(doc);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/insertMany")
    @ResponseBody
    public String insertMany() {
        List<Document> documentList = new ArrayList<>();
        Document doc = new Document("name", "manymanay").append("company", "ManyCompany");
        Document doc2 = new Document("name", "manymanay2").append("company", "ManyCompany2");
        documentList.add(doc);
        documentList.add(doc2);

        for (int i = 3; i < 100; i++)
            documentList.add(new Document("name", "manymanay" + i).append("company", "ManyCompany"));

        mongoService.addMany(documentList);

        return "OK";
    }

    @Description("java driver 3.0 client 로 insert")
    @RequestMapping(value = "/mongodb/insertWithClient30")
    @ResponseBody
    public String insertWithClient30() {

        Document doc = new Document("name", "pinpoint2").append("company", "Naver");
        mongoService.addWithClient30(doc);

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
    public String insertTo2ndserver() {

        Document doc = new Document("name", "pinpoint").append("company", "Naver");
        mongoService.addToSecondDB(doc);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/insertNested")
    @ResponseBody
    public String insertNested() {

        Document doc2 = new Document("name", "pinpoint2").append("company", new Document("nestedDoc", "1"));
        mongoService.add(doc2);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/insertArray")

    @ResponseBody
    public String insertArray() {

        BsonValue a = new BsonDouble(111);
        BsonValue b = new BsonDouble(222);
        BsonArray bsonArray = new BsonArray();
        bsonArray.add(a);
        bsonArray.add(b);

        Document doc = new Document("array", bsonArray);
        mongoService.add(doc);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/insertCollection")
    @ResponseBody
    public String insertCollection() {

        Document doc = new Document("Java_Collection", Arrays.asList("naver", "apple"));
        mongoService.add(doc);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/find")
    @ResponseBody
    public String find() {
        Document document = new Document().append("name", new BsonString("roy2"))
                .append("company", new BsonString("Naver2"));

        MongoCursor<Document> cursor = mongoService.find(document).iterator();

        try {
            while (cursor.hasNext()) {
                logger.info(cursor.next().toJson());
            }
        } finally {
            cursor.close();
        }
        return "OK";
    }

    @RequestMapping(value = "/mongodb/index")
    @ResponseBody
    public String index() {

        mongoService.createIndex(Indexes.ascending("name", "company"));

        List<IndexModel> indexes = new ArrayList<>();
        indexes.add(new IndexModel(Indexes.ascending("name2", "company")));
        indexes.add(new IndexModel(Indexes.ascending("name3", "company")));

        mongoService.createIndexes(indexes);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/update")
    @ResponseBody
    public String update() {

        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");
        Document doc2 = new Document("$set", new Document("name", "pinpoint2").append("company", "Naver2"));

        mongoService.update(doc1, doc2);
        //mongoService.updateWithClient( doc1, doc2); works with higher version of MongoDB

        return "OK";
    }

    @RequestMapping(value = "/mongodb/updateSimple")
    @ResponseBody
    public String updateWithSet() {

        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        mongoService.update(doc1, and(set("name", "pinpointWithSet"), set("company", "NaverWithSet")));

        return "OK";
    }

    @RequestMapping(value = "/mongodb/updateWithEach")
    @ResponseBody
    public String updateWithEach() {

        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        mongoService.update(doc1, addEachToSet("arrayField", asList("pinpoint", "pinpoint2")), new UpdateOptions().upsert(true));

        return "OK";
    }

    @RequestMapping(value = "/mongodb/updatePush")
    @ResponseBody
    public String updatePush() {

        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        mongoService.update(doc1, pushEach("arrayField", asList("pinpoint", "pinpoint2"), new PushOptions().position(1)));

        return "OK";
    }

    @RequestMapping(value = "/mongodb/updatePullAll")
    @ResponseBody
    public String updatePullAll() {

        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        mongoService.update(doc1, pullAll("arrayField", asList("pinpoint", "pinpoint2")));

        return "OK";
    }

    @RequestMapping(value = "/mongodb/updateComposite")
    @ResponseBody
    public String updateComposite() {

        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        mongoService.update(doc1, combine(asList(pushEach("arrayField", asList("pinpoint", "pinpoint2")), pushEach("arrayField", asList("pinpoint", "pinpoint2")))));

        return "OK";
    }

    @RequestMapping(value = "/mongodb/sortCompound")
    @ResponseBody
    public String sortCompound() {

        FindIterable<Document> iterable = mongoService.find(not(eq("name", "pinpoint"))).sort(Sorts.ascending("name", "name2"));

        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                logger.info(document.toJson());
            }
        };

        iterable.forEach(printBlock);
        return "OK";
    }


    @RequestMapping(value = "/mongodb/filterEQ")
    @ResponseBody
    public String filterEQ() {

        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");
        Document doc2 = new Document("$set", new Document("name", "pinpoint2").append("company", "Naver2"));

        mongoService.update(eq("name", "pinpoint"), doc2);
        return "OK";
    }

    @RequestMapping(value = "/mongodb/filterAND")
    @ResponseBody
    public String filterAND() {

        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");
        Document doc2 = new Document("$set", new Document("name", "pinpoint2").append("company", "Naver2"));

        mongoService.update(and(eq("name", "pinpoint"), eq("company", "Naver")), doc2);
        return "OK";
    }

    @RequestMapping(value = "/mongodb/filterNE")
    @ResponseBody
    public String filterNE() {

        Document doc2 = new Document("$set", new Document("name", "pinpoint2").append("company", "Naver2"));

        mongoService.update(and(ne("name", "pinpoint"), ne("company", "Naver")), doc2);
        return "OK";
    }

    @RequestMapping(value = "/mongodb/filterREGREX")
    @ResponseBody
    public String filterREGREX() {

        FindIterable<Document> iterable = mongoService.find(regex("name", "%inpoint", "i"));

        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                logger.info(document.toJson());
            }
        };

        iterable.forEach(printBlock);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/filterNOT")
    @ResponseBody
    public String filterNOT() {

        FindIterable<Document> iterable = mongoService.find(not(eq("name", "pinpoint")));

        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                logger.info(document.toJson());
            }
        };

        iterable.forEach(printBlock);
        return "OK";
    }

    @RequestMapping(value = "/mongodb/filterIN")
    @ResponseBody
    public String filterIN() {

        FindIterable<Document> iterable = mongoService.find(in("name", "pinpoint", "pinpoint2"));

        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                logger.info(document.toJson());
            }
        };

        iterable.forEach(printBlock);
        return "OK";
    }

    @RequestMapping(value = "/mongodb/filterGEO")
    @ResponseBody
    public String filterGEO() {

        String coords = "28.56402,79.93652a27.27569,26.16394a42.69404,20.02808a48.61541,51.37207a";
        String[] coors = coords.split("a");
        final List<List<Double>> polygons = new ArrayList<>();

        for (int i = 0; i < coors.length; i++) {
            String[] coo = coors[i].split(",");
            polygons.add(Arrays.asList(Double.parseDouble(coo[0]), Double.parseDouble(coo[1])));
        }

        FindIterable<Document> iterable = mongoService.find(Filters.geoWithinPolygon("loc", polygons));

        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                logger.info(document.toJson());
            }
        };

        iterable.forEach(printBlock);
        return "OK";
    }

    @RequestMapping(value = "/mongodb/filterTEXT")
    @ResponseBody
    public String filterTEXT() {

        FindIterable<Document> iterable = mongoService.find(Filters.text("bakery coffee"));

        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                logger.info(document.toJson());
            }
        };

        iterable.forEach(printBlock);
        return "OK";
    }

    @RequestMapping(value = "/mongodb/filterORNOR")
    @ResponseBody
    public String filterORNOR() {
        FindIterable<Document> iterable = mongoService.find(
                and(
                        or(eq("name", "pinpoint"), eq("company", "Naver")),
                        nor(eq("name", "pinpoint"), eq("company", "Naver"))
                )
        );

        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                logger.info(document.toJson());
            }
        };

        iterable.forEach(printBlock);
        return "OK";
    }


    @RequestMapping(value = "/mongodb/delete")
    @ResponseBody
    public String delete() {

        Document doc = new Document("name", "pinpoint").append("company", "Naver");
        DeleteResult deleteResult = mongoService.delete(doc);

        logger.info("Deleted : " + deleteResult.getDeletedCount());
        return "OK";
    }

    @RequestMapping(value = "/mongodb/close")
    @ResponseBody
    public String close() {

        mongoService.close();

        return "OK";
    }
}
