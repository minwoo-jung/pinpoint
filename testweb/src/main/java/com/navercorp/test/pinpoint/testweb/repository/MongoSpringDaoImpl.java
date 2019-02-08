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

package com.navercorp.test.pinpoint.testweb.repository;

import com.navercorp.test.pinpoint.testweb.document.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public class MongoSpringDaoImpl implements MongoSpringDao {

    @Autowired
    MongoTemplate mongoTemplate;

    private static final String COLLECTION_NAME = "user";

    public List<User> listUser() {
        BasicQuery query1 = (BasicQuery) new BasicQuery("{  }").limit(2);
        return mongoTemplate.find(query1, User.class);
    }

    public void add(User user) {
        if (!mongoTemplate.collectionExists(User.class)) {
            mongoTemplate.createCollection(User.class);

        }
        user.setId(UUID.randomUUID().toString());
        mongoTemplate.insert(user, COLLECTION_NAME);
        mongoTemplate.insert(user, "user2");
    }

    public void update(User user) {
        mongoTemplate.save(user);
    }

    public void delete(User user) {
        mongoTemplate.remove(user, COLLECTION_NAME);
    }

    public User findUserById(String id) {
        return mongoTemplate.findById(id, User.class);
    }
}
