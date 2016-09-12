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

import java.io.InputStream;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class DBUnitSupport {

    enum DataType {
        EXCEL, FLATXML
    }

    @Autowired
    private DataSource dataSource;

    protected void cleanInsertXmlData(String fileSource) {
        insertData(fileSource, DataType.FLATXML, DatabaseOperation.CLEAN_INSERT);
    }

    protected void cleanInsertXlsData(String fileSource) {
        insertData(fileSource, DataType.EXCEL, DatabaseOperation.CLEAN_INSERT);
    }

    private void insertData(String fileSource, DataType type,
            DatabaseOperation operation) {
        try {
            InputStream sourceStream = new ClassPathResource(fileSource,
                    getClass()).getInputStream();
            IDataSet dataset = null;
            if (type == DataType.EXCEL) {
                dataset = new XlsDataSet(sourceStream);
            } else if (type == DataType.FLATXML) {
                dataset = new FlatXmlDataSet(sourceStream);
            }
            operation.execute(
                    new DatabaseConnection(DataSourceUtils
                            .getConnection(dataSource)), dataset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void insertXmlData(String fileSource) {
        insertData(fileSource, DataType.FLATXML, DatabaseOperation.INSERT);
    }

    protected void insertXlsData(String fileSource) {
        insertData(fileSource, DataType.EXCEL, DatabaseOperation.INSERT);
    }
}