package com.navercorp.pinpoint.testweb.repository;

/**
 *
 */
public interface CubridDao {
    int selectOne();

    int selectOneWithParam(int id);

    boolean createStatement();
    
    boolean createErrorStatement();
}
