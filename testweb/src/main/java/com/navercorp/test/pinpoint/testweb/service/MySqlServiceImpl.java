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

package com.navercorp.test.pinpoint.testweb.service;

import com.navercorp.test.pinpoint.testweb.repository.MySqlDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Service
@Transactional("mysqlTransactionManager")
public class MySqlServiceImpl implements MySqlService {

    @Autowired
    private MySqlDao mysqlDao;

    @Override
    public int selectOne() {
        return mysqlDao.selectOne();
    }

    @Override
    public int selectOneWithParam(int id) {
        return mysqlDao.selectOneWithParam(id);
    }

    @Override
    public String concat(char a, char b) {
        return mysqlDao.callConcat(a, b);
    }

    @Override
    public int swapAndGetSum(int a, int b) {
        return mysqlDao.callSwapAndGetSum(a, b);
    }

    @Override
    public void createStatement() {
        mysqlDao.createStatement();
    }
}
