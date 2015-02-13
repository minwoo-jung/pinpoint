package com.navercorp.pinpoint.testweb.service;

public interface MemcachedService {

    void set();
    
    void get();
    
    void delete();
    
    void timeout();
    
}
