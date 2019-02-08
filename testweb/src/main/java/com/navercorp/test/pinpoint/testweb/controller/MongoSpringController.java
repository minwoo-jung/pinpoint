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

import com.navercorp.test.pinpoint.testweb.document.User;
import com.navercorp.test.pinpoint.testweb.document.User2;
import com.navercorp.test.pinpoint.testweb.repository.User2Repository;
import com.navercorp.test.pinpoint.testweb.repository.UserRepository;
import com.navercorp.test.pinpoint.testweb.service.MongoSpringService;
import com.navercorp.test.pinpoint.testweb.util.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@EnableMongoRepositories(basePackageClasses = UserRepository.class)
@Controller
public class MongoSpringController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MongoSpringService mongoSpringService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private User2Repository user2Repository;

    @Description("user save with repository")
    @RequestMapping(value = "/mongodb/saveUserWithRepository")
    @ResponseBody
    public String saveUserWithRepository() {

        User user = new User("1", "Peter", "Development", 3000L);
        User2 user2 = new User2("333", "Peter", "Development", 3000L);

        userRepository.insert(user);
        user2Repository.insert(user2);
        return "OK";
    }

    @Description("user get with repository")
    @RequestMapping(value = "/mongodb/getUserWithRepository")
    @ResponseBody
    public List<User> getAllUsersWithRepository() {

        return userRepository.findAll();
    }

    @Description("user save")
    @RequestMapping(value = "/mongodb/saveUser")
    @ResponseBody
    public String saveUser() {

        User user = new User("2", "Sam", "Operations", 2000L);

        mongoSpringService.add(user);
        return "OK";
    }

    @Description("user get")
    @RequestMapping(value = "/mongodb/getUser")
    @ResponseBody
    public List<User> getAllUsers() {

        return mongoSpringService.listUser();
    }
}
