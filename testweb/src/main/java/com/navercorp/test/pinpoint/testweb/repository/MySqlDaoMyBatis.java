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

package com.navercorp.test.pinpoint.testweb.repository;

import com.navercorp.test.pinpoint.testweb.domain.ConcatProcedureParam;
import com.navercorp.test.pinpoint.testweb.domain.SwapProcedureParam;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 */
@Repository
public class MySqlDaoMyBatis implements MySqlDao {

    @Autowired
    @Qualifier("mysqlSqlMapClientTemplate")
    private SqlSessionTemplate sqlMapClientTemplate;

    @Autowired
    @Qualifier("mysqlDataSource")
    private DataSource datasource;

    @Override
    public int selectOne() {
        return (Integer) sqlMapClientTemplate.selectOne("selectOne");
    }

    @Override
    public int selectOneWithParam(int id) {
        return (Integer) sqlMapClientTemplate.selectOne("selectOneWithParam", id);
    }

    @Override
    public String callConcat(char a, char b) {
        ConcatProcedureParam param = new ConcatProcedureParam();
        param.setA(a);
        param.setB(b);
        sqlMapClientTemplate.update("concatCharacters", param);
        return param.getC();
    }

    @Override
    public int callSwapAndGetSum(int a, int b) {
        SwapProcedureParam param = new SwapProcedureParam();
        param.setA(a);
        param.setB(b);
        int sum = sqlMapClientTemplate.selectOne("swapAndGetSum", param);
        if (param.getA() != b || param.getB() != a) {
            return -1;
        }
        return sum;
    }

    @Override
    public boolean createStatement() {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = datasource.getConnection();
            statement = connection.createStatement();
            return statement.execute("select 1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }

        }
    }

//    private String createConcatProcedure() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("  CREATE OR REPLACE PROCEDURE concatCharacters(");
//        sb.append("    IN  a CHAR(1),");
//        sb.append("    IN  b CHAR(1),");
//        sb.append("    OUT c CHAR(2)");
//        sb.append("  )");
//        sb.append("  BEGIN");
//        sb.append("    SET c = CONCAT(a, b);");
//        sb.append("  END");
//        return sb.toString();
//    }

//    private String createSwapAndGetSumProcedure() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("  CREATE OR REPLACE PROCEDURE swapAndGetSum(");
//        sb.append("    INOUT a INT,");
//        sb.append("    INOUT b INT");
//        sb.append("  )");
//        sb.append("  BEGIN");
//        sb.append("    DECLARE temp INT;");
//        sb.append("    SET temp = a;");
//        sb.append("    SET a = b;");
//        sb.append("    SET b = temp;");
//        sb.append("    SELECT temp + a;");
//        sb.append("  END");
//        return sb.toString();
//    }

}
