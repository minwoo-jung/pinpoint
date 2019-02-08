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

import com.navercorp.test.pinpoint.testweb.document.User;
import com.navercorp.test.pinpoint.testweb.repository.MongoSpringDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoSpringServiceImpl implements MongoSpringService {

    @Autowired
    MongoSpringDao mongoSpringDao;

    public List<User> listUser() {
        return mongoSpringDao.listUser();
    }

    public void add(User user) {
        mongoSpringDao.add(user);
    }

    public void update(User user) {
        mongoSpringDao.update(user);
    }

    public void delete(User user) {
        mongoSpringDao.delete(user);
    }

    public User findUserById(String id) {
        return mongoSpringDao.findUserById(id);
    }
}
