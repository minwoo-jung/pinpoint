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
 */
package com.navercorp.pinpoint.web.jdbc;

import com.navercorp.pinpoint.web.namespace.NameSpaceInfo;
import org.apache.hadoop.security.SaslOutputStream;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author minwoo.jung
 */
// 성능상 문제 있는 코드고 일단 동작 시연을 위해서 만듬.
public class PaaSConnectionCreator implements ConnectionCreator {

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public Connection createConnection(Connection connection) throws SQLException {
        System.out.println("this :" + connection.toString());
        System.out.println("~~~~~~~~~~~~~~~~~~start getConnection(1) ~~~~~~~~~~~~~~~~~~~~~~~~~~~~ : " + connection.getCatalog());
        try {
            NameSpaceInfo nameSpaceInfo = applicationContext.getBean(NameSpaceInfo.class);
            System.out.println("!!!!!!!!!!!!!!!!!!!! : namespace Info" + nameSpaceInfo);
            connection.setCatalog(nameSpaceInfo.getMysqlDatabaseName());
        } catch (Exception e) {
            e.printStackTrace();
            connection.setCatalog("naver");
        }
        System.out.println("~~~~~~~~~~~~~~~~~ set catalog  (2) ~~~~~~~~~~~~~~~~~~~~~~~~~~~~ : " + connection.getCatalog());
        return new NaverConnectionDelegator(connection);
    }
}
