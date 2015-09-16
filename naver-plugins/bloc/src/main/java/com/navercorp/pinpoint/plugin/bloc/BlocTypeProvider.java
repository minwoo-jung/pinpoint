package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

public class BlocTypeProvider implements TraceMetadataProvider, BlocConstants {
    
    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(BLOC);
        context.addServiceType(BLOC_INTERNAL_METHOD, AnnotationKeyMatchers.exact(BlocConstants.CALL_URL));

        context.addAnnotationKey(CALL_URL);
        context.addAnnotationKey(CALL_PARAM);
        context.addAnnotationKey(PROTOCOL);
    }
}