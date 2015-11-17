package com.navercorp.pinpoint.testweb.service;

/**
 *
 */
public interface MsSqlServerService {

    int selectOne();

    int selectOneWithParam(int id);

    void createStatement();

}
