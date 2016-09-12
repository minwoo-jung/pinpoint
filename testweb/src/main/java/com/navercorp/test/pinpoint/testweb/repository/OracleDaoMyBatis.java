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
public class OracleDaoMyBatis implements OracleDao {

    @Autowired
    @Qualifier("oracleSqlMapClientTemplate")
    private SqlSessionTemplate sqlMapClientTemplate;

    @Autowired
    @Qualifier("oracleDataSource")
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
        OracleSwapProcedureParam param = new OracleSwapProcedureParam();
        param.setA(a);
        param.setB(b);
        sqlMapClientTemplate.selectOne("swapAndGetSum", param);
        int sum = param.getC();
        if (param.getA() != b || param.getB() != a) {
            return -1;
        }
        return sum;
    }

    public static class OracleSwapProcedureParam extends SwapProcedureParam {
        private int c;

        public int getC() {
            return c;
        }

        public void setC(int c) {
            this.c = c;
        }
    }

    @Override
    public boolean createStatement() {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = datasource.getConnection();
            statement = connection.createStatement();
            return statement.execute("select 1 from dual");

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
//        sb.append("    a IN  CHAR,");
//        sb.append("    b IN  CHAR,");
//        sb.append("    c OUT CHAR");
//        sb.append("  )");
//        sb.append("  AS");
//        sb.append("  BEGIN");
//        sb.append("    c := a || b;");
//        sb.append("  END concatCharacters;");
//        return sb.toString();
//    }

//    private String createSwapAndGetSumProcedure() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("  CREATE OR REPLACE PROCEDURE swapAndGetSum(");
//        sb.append("    a IN OUT NUMBER,");
//        sb.append("    b IN OUT NUMBER,");
//        sb.append("    c OUT    NUMBER");
//        sb.append("  )");
//        sb.append("  AS");
//        sb.append("  BEGIN");
//        sb.append("    c := a;");
//        sb.append("    a := b;");
//        sb.append("    b := c;");
//        sb.append("    SELECT c + a INTO c FROM DUAL;");
//        sb.append("  END swapAndGetSum;");
//        return sb.toString();
//    }

}
