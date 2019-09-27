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
package com.navercorp.pinpoint.manager.dao.mysql;

import com.navercorp.pinpoint.manager.core.StorageStatus;
import com.navercorp.pinpoint.manager.dao.MetadataDao;
import com.navercorp.pinpoint.manager.dao.mybatis.MetadataMapper;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.DatabaseManagement;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.HbaseManagement;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.PaaSOrganizationInfo;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.PaaSOrganizationKey;
import com.navercorp.pinpoint.manager.domain.mysql.metadata.RepositoryInfo;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
@Repository
public class MysqlMetadataDao implements MetadataDao {

    private static final String NAMESPACE = MetadataDao.class.getPackage().getName() + "." + MetadataDao.class.getSimpleName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    public MysqlMetadataDao(@Qualifier("metaDataSqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate must not be null");
    }

    @Override
    public boolean existDatabase(String databaseName) {
        return sqlSessionTemplate.selectOne(MetadataMapper.class.getName() + ".selectDatabaseName", databaseName) != null;
    }

    @Override
    public boolean createDatabase(String databaseName) {
        int result = sqlSessionTemplate.insert(MetadataMapper.class.getName() + ".createDatabase", databaseName);
        if (result > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean dropDatabase(String databaseName) {
        int result = sqlSessionTemplate.update(MetadataMapper.class.getName() + ".dropDatabase", databaseName);
        if (result > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean existOrganization(String organizationName) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "existOrganization", organizationName);
    }

    @Override
    public boolean insertPaaSOrganizationInfo(PaaSOrganizationInfo paaSOrganizationInfo) {
        int result = sqlSessionTemplate.insert(NAMESPACE + "insertOrganizationInfo", paaSOrganizationInfo);
        if (result > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean deletePaaSOrganizationInfo(String organizationName) {
        final String sqlId = NAMESPACE + "deleteOrganizationInfo";
        int affectedRows = sqlSessionTemplate.delete(sqlId, organizationName);
        if (affectedRows == 1) {
            return true;
        } else if (affectedRows == 0) {
            return false;
        }
        throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sqlId, 1, affectedRows);
    }

    @Override
    public PaaSOrganizationInfo selectPaaSOrganizationInfo(String organizationName) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectOrganizationInfo", organizationName);
    }

    @Override
    public boolean updatePaaSOrganizationInfo(PaaSOrganizationInfo paaSOrganizationInfo) {
        final String sqlId = NAMESPACE + "updateOrganizationInfo";
        int affectedRows = sqlSessionTemplate.update(sqlId, paaSOrganizationInfo);
        if (affectedRows == 1) {
            return true;
        } else if (affectedRows == 0) {
            return false;
        }
        throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sqlId, 1, affectedRows);
    }

    @Override
    public List<String> selectAllOrganizationNames() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectAllOrganizationNames");
    }

    @Override
    public List<RepositoryInfo> selectAllRepositoryInfo() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectAllRepositoryInfo");
    }

    @Override
    public RepositoryInfo selectRepositoryInfo(String organizationName) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectRepositoryInfo", organizationName);
    }

    @Override
    public boolean existPaaSOrganizationKey(String organizationKey) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "existOrganizationKey", organizationKey);
    }

    @Override
    public boolean insertPaaSOrganizationKey(PaaSOrganizationKey paaSOrganizationKey) {
        int result = sqlSessionTemplate.insert(NAMESPACE + "insertOrganizationKey", paaSOrganizationKey);
        if (result > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean deletePaaSOrganizationKey(String organizationName) {
        final String sqlId = NAMESPACE + "deleteOrganizationKey";
        int affectedRows = sqlSessionTemplate.delete(sqlId, organizationName);
        if (affectedRows == 1) {
            return true;
        } else if (affectedRows == 0) {
            return false;
        }
        throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sqlId, 1, affectedRows);
    }

    @Override
    public PaaSOrganizationKey selectPaaSOrganizationKey(String key) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectOrganizationKey", key);
    }

    @Override
    public DatabaseManagement selectDatabaseManagement(String databaseName) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectDatabaseManagement", databaseName);
    }

    @Override
    public DatabaseManagement selectDatabaseManagementForUpdate(String databaseName) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectDatabaseManagementForUpdate", databaseName);
    }

    @Override
    public boolean insertDatabaseManagement(DatabaseManagement databaseManagement) {
        int result = sqlSessionTemplate.insert(NAMESPACE + "insertDatabaseManagement", databaseManagement);
        if (result == 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean updateDatabaseStatus(String databaseName, StorageStatus databaseStatus) {
        DatabaseManagement databaseManagement = new DatabaseManagement();
        databaseManagement.setDatabaseName(databaseName);
        databaseManagement.setDatabaseStatus(databaseStatus);
        final String sqlId = NAMESPACE + "updateDatabaseStatus";
        int affectedRows = sqlSessionTemplate.update(sqlId, databaseManagement);
        if (affectedRows == 1) {
            return true;
        } else if (affectedRows == 0) {
            return false;
        }
        throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sqlId, 1, affectedRows);
    }

    @Override
    public boolean deleteDatabaseManagement(String databaseName) {
        final String sqlId = NAMESPACE + "deleteDatabaseManagement";
        int affectedRows = sqlSessionTemplate.delete(sqlId, databaseName);
        if (affectedRows == 1) {
            return true;
        } else if (affectedRows == 0) {
            return false;
        }
        throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sqlId, 1, affectedRows);
    }

    @Override
    public HbaseManagement selectHbaseManagement(String hbaseNamespace) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectHbaseManagement", hbaseNamespace);
    }

    @Override
    public HbaseManagement selectHbaseManagementForUpdate(String hbaseNamespace) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectHbaseManagementForUpdate", hbaseNamespace);
    }

    @Override
    public boolean insertHbaseManagement(HbaseManagement hbaseManagement) {
        int result = sqlSessionTemplate.insert(NAMESPACE + "insertHbaseManagement", hbaseManagement);
        if (result == 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean updateHbaseStatus(String hbaseNamespace, StorageStatus hbaseStatus) {
        HbaseManagement hbaseManagement = new HbaseManagement();
        hbaseManagement.setHbaseNamespace(hbaseNamespace);
        hbaseManagement.setHbaseStatus(hbaseStatus);
        String sqlId = NAMESPACE + "updateHbaseStatus";
        int affectedRows = sqlSessionTemplate.update(sqlId, hbaseManagement);
        if (affectedRows == 1) {
            return true;
        } else if (affectedRows == 0) {
            return false;
        }
        throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sqlId, 1, affectedRows);
    }

    @Override
    public boolean deleteHbaseManagement(String hbaseNamespace) {
        final String sqlId = NAMESPACE + "deleteHbaseManagement";
        int affectedRows = sqlSessionTemplate.delete(sqlId, hbaseNamespace);
        if (affectedRows == 1) {
            return true;
        } else if (affectedRows == 0) {
            return false;
        }
        throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sqlId, 1, affectedRows);
    }
}
