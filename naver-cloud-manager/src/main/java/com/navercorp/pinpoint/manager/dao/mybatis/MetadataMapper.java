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
package com.navercorp.pinpoint.manager.dao.mybatis;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;

/**
 * @author minwoo.jung
 */
public interface MetadataMapper {

    @SelectProvider(type = MetadataProvider.class, method = "selectDatabaseName")
    String selectDatabaseName(String databaseName);

    @InsertProvider(type = MetadataProvider.class, method = "createDatabase")
    int createDatabase(String databaseName);

    @DeleteProvider(type = MetadataProvider.class, method = "dropDatabase")
    int dropDatabase(String databaseName);
}
