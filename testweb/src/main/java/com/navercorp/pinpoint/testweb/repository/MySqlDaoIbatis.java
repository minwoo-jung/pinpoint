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
public class MySqlDaoIbatis implements MySqlDao {

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
                } catch (SQLException e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }

        }
    }
}
