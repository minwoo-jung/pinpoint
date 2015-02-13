package com.navercorp.pinpoint.testweb.service;

public interface NpcService {

    void invoke();
    
    void keepalive();
    
    void factory();
    
    void lightweight();
    
    void listener(Runnable callback);
}
