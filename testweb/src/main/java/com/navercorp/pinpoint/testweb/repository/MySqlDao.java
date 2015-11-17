package com.navercorp.pinpoint.testweb.repository;

/**
 *
 */
public interface MySqlDao {

    int selectOne();

    int selectOneWithParam(int id);

    boolean createStatement();
}
