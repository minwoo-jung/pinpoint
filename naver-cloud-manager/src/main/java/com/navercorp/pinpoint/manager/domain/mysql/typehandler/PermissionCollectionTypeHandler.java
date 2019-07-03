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

package com.navercorp.pinpoint.manager.domain.mysql.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.manager.domain.mysql.repository.role.PermissionCollection;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author HyunGil Jeong
 */
@MappedJdbcTypes({JdbcType.VARCHAR})
@MappedTypes(PermissionCollection.class)
public class PermissionCollectionTypeHandler implements TypeHandler<PermissionCollection> {

    private final static ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, PermissionCollection permissionCollection, JdbcType jdbcType) throws SQLException {
        String permissionCollectionJson = serialize(permissionCollection);
        ps.setString(i, permissionCollectionJson);
    }

    @Override
    public PermissionCollection getResult(ResultSet rs, String columnName) throws SQLException {
        String permissionCollectionJson = rs.getString(columnName);
        return deserialize(permissionCollectionJson);
    }

    @Override
    public PermissionCollection getResult(ResultSet rs, int columnIndex) throws SQLException {
        String permissionCollectionJson = rs.getString(columnIndex);
        return deserialize(permissionCollectionJson);
    }

    @Override
    public PermissionCollection getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String permissionCollectionJson = cs.getString(columnIndex);
        return deserialize(permissionCollectionJson);
    }

    private String serialize(PermissionCollection permissionCollection) {
        try {
            return OBJECT_MAPPER.writeValueAsString(permissionCollection);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing PermissionCollection : {}", permissionCollection, e);
            throw new RuntimeException("Error serializing PermissionCollection", e);
        }
    }

    private PermissionCollection deserialize(String serializedPermissionCollection) {
        try {
            return OBJECT_MAPPER.readValue(serializedPermissionCollection, PermissionCollection.class);
        } catch (IOException e) {
            logger.error("Error deserializing PermissionCollection json : {}", serializedPermissionCollection, e);
            throw new RuntimeException("Error deserializing PermissionCollection", e);
        }
    }
}
