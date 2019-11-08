/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author HyunGil Jeong
 */
public class NssAuthDaoFactory implements FactoryBean<NssAuthDao> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("propsNssAuthDao")
    private NssAuthDao propsNssAuthDao;

    @Autowired
    @Qualifier("mysqlNssAuthDao")
    private NssAuthDao mysqlNssAuthDao;

    @Value("${nss.use.properties:false}")
    private boolean useProperties;

    @Override
    public NssAuthDao getObject() throws Exception {
        logger.info("nss.use.properties set to {}", useProperties);
        if (useProperties) {
            logger.info("Configuring nss authorization using properties file");
            return propsNssAuthDao;
        } else {
            logger.info("Configuring nss authorization using mysql");
            return mysqlNssAuthDao;
        }
    }

    @Override
    public Class<?> getObjectType() {
        return NssAuthDao.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
