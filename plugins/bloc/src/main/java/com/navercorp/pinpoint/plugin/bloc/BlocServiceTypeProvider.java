package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.common.plugin.ServiceTypeProvider;
import com.navercorp.pinpoint.common.plugin.ServiceTypeSetupContext;

public class BlocServiceTypeProvider implements ServiceTypeProvider, BlocConstants {
    
    @Override
    public void setUp(ServiceTypeSetupContext context) {
        context.addServiceType(BLOC, BLOC_INTERNAL_METHOD);
        context.addAnnotationKey(CALL_URL, CALL_PARAM, PROTOCOL);
    }
}