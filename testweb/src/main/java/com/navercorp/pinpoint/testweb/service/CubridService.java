package com.navercorp.pinpoint.testweb.service;

/**
 *
 */
public interface CubridService {

    int selectOne();

    int selectOneWithParam(int id);

    void createStatement();

    void createErrorStatement();
}
