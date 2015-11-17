package com.navercorp.pinpoint.testweb.repository;

/**
 *
 */
public interface OracleDao {
    int selectOne();

    int selectOneWithParam(int id);

    boolean createStatement();
}
