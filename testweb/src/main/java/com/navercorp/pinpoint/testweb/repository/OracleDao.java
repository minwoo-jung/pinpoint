package com.navercorp.pinpoint.testweb.repository;

/**
 *
 */
public interface OracleDao {
    int selectOne();

    int selectOneWithParam(int id);

    String callConcat(char a, char b);

    int callSwapAndGetSum(int a, int b);

    boolean createStatement();
}
