package com.navercorp.pinpoint.testweb.service;

/**
 *
 */
public interface MySqlService {

    int selectOne();

    int selectOneWithParam(int id);

    String concat(char a, char b);

    int swapAndGetSum(int a, int b);

    void createStatement();

}
