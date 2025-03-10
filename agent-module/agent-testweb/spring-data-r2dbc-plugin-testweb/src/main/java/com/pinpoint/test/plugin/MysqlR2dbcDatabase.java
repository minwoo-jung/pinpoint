/*
 * Copyright 2022 NAVER Corp.
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

package com.pinpoint.test.plugin;

import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.asyncer.r2dbc.mysql.constant.SslMode;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class MysqlR2dbcDatabase implements R2dbcDatabase {
    private ConnectionFactory connectionFactory;

    public MysqlR2dbcDatabase() {
        System.out.println("INIT");
        MySqlConnectionConfiguration connectionConfiguration = MySqlConnectionConfiguration.builder()
                .host("localhost")
                .port(32783)
                .database("test")
                .user("root")
                .password("")
                .connectTimeout(Duration.ofSeconds(5 * 60))
                .sslMode(SslMode.DISABLED)
                .build();
        connectionFactory = MySqlConnectionFactory.from(connectionConfiguration);
    }

    @Override
    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }
}
