package com.navercorp.pinpoint.testweb.repository;

/**
 *
 */
public interface CubridDao {
    int selectOne();

    boolean createStatement();
    
    boolean createErrorStatement();
}
