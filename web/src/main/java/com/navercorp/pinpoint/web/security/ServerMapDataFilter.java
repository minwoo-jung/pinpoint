package com.navercorp.pinpoint.web.security;

import com.navercorp.pinpoint.web.vo.Application;

public interface ServerMapDataFilter {
    
    boolean filter(Application application);
    
}
