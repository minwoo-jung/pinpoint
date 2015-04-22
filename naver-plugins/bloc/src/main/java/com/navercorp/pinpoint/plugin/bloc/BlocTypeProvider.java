package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

public class BlocTypeProvider implements TraceMetadataProvider, BlocConstants {
    
    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(BLOC);
        context.addServiceType(BLOC_INTERNAL_METHOD, new AnnotationKeyMatcher.ExactMatcher(BlocConstants.CALL_URL));

        context.addAnnotationKey(CALL_URL);
        context.addAnnotationKey(CALL_PARAM);
        context.addAnnotationKey(PROTOCOL);
    }
}