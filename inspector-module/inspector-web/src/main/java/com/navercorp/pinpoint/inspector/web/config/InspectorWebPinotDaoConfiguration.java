/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.inspector.web.config;

import com.navercorp.pinpoint.metric.collector.config.MyBatisRegistryHandler;
import com.navercorp.pinpoint.metric.web.config.WebRegistryHandler;
import com.navercorp.pinpoint.pinot.mybatis.MyBatisConfiguration;
import com.navercorp.pinpoint.pinot.mybatis.PinotAsyncTemplate;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;

/**
 * @author minwoo.jung
 */
@org.springframework.context.annotation.Configuration
public class InspectorWebPinotDaoConfiguration {
    private final Logger logger = LogManager.getLogger(InspectorWebPinotDaoConfiguration.class);

    @Bean
    public TransactionManager pinotTransactionManager(@Qualifier("pinotDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public FactoryBean<SqlSessionFactory> sqlPinotSessionFactory(
            @Qualifier("pinotDataSource") DataSource dataSource,
            @Value("classpath*:/inspector/web/mapper/pinot/*Mapper.xml") Resource[] mappers) {

        for (Resource mapper : mappers) {
            logger.info("Mapper location: {}", mapper.getDescription());
        }

        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setMapperLocations(mappers);
        sessionFactoryBean.setTransactionFactory(transactionFactory());

        Configuration config = MyBatisConfiguration.defaultConfiguration();
        sessionFactoryBean.setConfiguration(config);

        MyBatisRegistryHandler registry = registryHandler();
        registry.registerTypeAlias(config.getTypeAliasRegistry());
        registry.registerTypeHandler(config.getTypeHandlerRegistry());

        return sessionFactoryBean;
    }

    private TransactionFactory transactionFactory() {
        return new ManagedTransactionFactory();
    }

    private MyBatisRegistryHandler registryHandler() {
        return new WebRegistryHandler();
    }


    @Bean
    public SqlSessionTemplate sqlPinotSessionTemplate(
            @Qualifier("sqlPinotSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }

    @Bean
    public PinotAsyncTemplate pinotAsyncTemplate(
            @Qualifier("sqlPinotSessionFactory") SqlSessionFactory sessionFactory) {
        return new PinotAsyncTemplate(sessionFactory);
    }

}
