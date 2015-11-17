package com.navercorp.pinpoint.testweb.service;

/**
 *
 */
public interface OracleService {

    int selectOne();

    int selectOneWithParam(int id);

    void createStatement();

}
