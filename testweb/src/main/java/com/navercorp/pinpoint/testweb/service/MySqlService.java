package com.navercorp.pinpoint.testweb.service;

/**
 *
 */
public interface MySqlService {

    int selectOne();

    int selectOneWithParam(int id);

    void createStatement();

}
