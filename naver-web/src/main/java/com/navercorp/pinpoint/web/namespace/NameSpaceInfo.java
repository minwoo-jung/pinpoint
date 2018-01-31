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
package com.navercorp.pinpoint.web.namespace;

/**
 * @author minwoo.jung
 */
public class NameSpaceInfo {

    private volatile String userId = "";
    private volatile String mysqlDatabaseName = "";
    private volatile String hbaseNamespace = "";
    private volatile boolean init = false;

    public synchronized void initNameSpaceInfo (String userid, String mysqlDatabaseName, String hbaseNamespace) {
        if (init == false) {
            this.userId = userid;
            this.hbaseNamespace = hbaseNamespace;
            this.mysqlDatabaseName = mysqlDatabaseName;
            init = true;
        }
    }

    public String getHbaseNamespace() {
        return hbaseNamespace;
    }

    public boolean isInit() {
        return init;
    }

    public String getMysqlDatabaseName() {
        return mysqlDatabaseName;
    }

    public String getUserId() {
        return userId;
    }


}
