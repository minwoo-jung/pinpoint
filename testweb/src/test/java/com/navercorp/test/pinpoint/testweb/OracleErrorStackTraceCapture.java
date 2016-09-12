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

package com.navercorp.test.pinpoint.testweb;

import oracle.jdbc.driver.OracleDriver;

import java.sql.SQLException;
import java.util.Properties;

/**
 *
 */
public class OracleErrorStackTraceCapture {

//    @Test
    public void errorConnect() throws SQLException {
        String url = "jdbc:oracle:thin:@10.98.133.173:1725:INDEV";
        Properties properties = new Properties();

        properties.setProperty("user", "lucy1");
        properties.setProperty("password", "lucy");

        OracleDriver oracleDriver = new OracleDriver();

        oracleDriver.connect(url, properties);


//        DriverManager.getConnection(url, "lucy", "lucy");



    }
}
