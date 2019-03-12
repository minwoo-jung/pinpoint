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
 */

package com.navercorp.pinpoint.web.dao.mysql;

import com.navercorp.pinpoint.web.dao.RoleDao;
import com.navercorp.pinpoint.web.namespace.RequestContextInitializer;
import com.navercorp.pinpoint.web.vo.role.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-web-naver.xml")
@WebAppConfiguration
public class MysqlRoleDaoTest extends RequestContextInitializer {

    @Autowired
    MysqlRoleDao dao;

    @Autowired
    @Qualifier("transactionManager")
    DataSourceTransactionManager transactionManager;

    @Test
    public void insertAndDeleteRoleInformationWithTx() {
        TransactionDefinition txDef = new DefaultTransactionDefinition();
        TransactionStatus txStatus = transactionManager.getTransaction(txDef);

        try {
            insertAndDelete();
            insertAndDelete02();
        } finally {
            transactionManager.rollback(txStatus);
        }
    }

    private void insertAndDelete() {
        PermsGroupAdministration permsGroupAdministration = new PermsGroupAdministration(false, true, true, false);
        PermsGroupAppAuthorization permsGroupAppAuthorization = new PermsGroupAppAuthorization(false, false, false);
        PermsGroupAlarm permsGroupAlarm = new PermsGroupAlarm(false, false);
        PermsGroupUserGroup permsGroupUserGroup = new PermsGroupUserGroup(false, false);
        PermissionCollection permissionCollection = new PermissionCollection(permsGroupAdministration, permsGroupAppAuthorization, permsGroupAlarm, permsGroupUserGroup);
        RoleInformation roleinformation = new RoleInformation("test_admin", permissionCollection);
        dao.insertRoleInformation(roleinformation);
    }

    private void insertAndDelete02() {
        PermsGroupAdministration permsGroupAdministration = new PermsGroupAdministration(false, false, false, true);
        PermsGroupAppAuthorization permsGroupAppAuthorization = new PermsGroupAppAuthorization(false, false, false);
        PermsGroupAlarm permsGroupAlarm = new PermsGroupAlarm(false, false);
        PermsGroupUserGroup permsGroupUserGroup = new PermsGroupUserGroup(false, false);
        PermissionCollection permissionCollection = new PermissionCollection(permsGroupAdministration, permsGroupAppAuthorization, permsGroupAlarm, permsGroupUserGroup);
        RoleInformation roleinformation = new RoleInformation("test_user", permissionCollection);
        dao.insertRoleInformation(roleinformation);
    }

}