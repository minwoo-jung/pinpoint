package com.navercorp.pinpoint.testweb.repository;

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
}
