package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

public class BlocTypeProvider implements TraceMetadataProvider {
    
    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(BlocConstants.BLOC);
        context.addServiceType(BlocConstants.BLOC_INTERNAL_METHOD, AnnotationKeyMatchers.exact(BlocConstants.CALL_URL));

        context.addAnnotationKey(BlocConstants.CALL_URL);
        context.addAnnotationKey(BlocConstants.CALL_PARAM);
        context.addAnnotationKey(BlocConstants.PROTOCOL);
    }
}