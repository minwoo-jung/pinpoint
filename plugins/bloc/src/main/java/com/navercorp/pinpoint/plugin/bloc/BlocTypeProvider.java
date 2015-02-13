package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.common.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.plugin.TypeProvider;
import com.navercorp.pinpoint.common.plugin.TypeSetupContext;

public class BlocTypeProvider implements TypeProvider, BlocConstants {
    
    @Override
    public void setUp(TypeSetupContext context) {
        context.addType(BLOC);
        context.addType(BLOC_INTERNAL_METHOD, new AnnotationKeyMatcher.ExactMatcher(BlocConstants.CALL_URL));

        context.addAnnotationKey(CALL_URL);
        context.addAnnotationKey(CALL_PARAM);
        context.addAnnotationKey(PROTOCOL);
    }
}