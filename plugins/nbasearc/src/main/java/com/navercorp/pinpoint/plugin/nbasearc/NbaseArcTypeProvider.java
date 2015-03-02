package com.navercorp.pinpoint.plugin.nbasearc;

import com.navercorp.pinpoint.common.plugin.TypeProvider;
import com.navercorp.pinpoint.common.plugin.TypeSetupContext;

public class NbaseArcTypeProvider implements TypeProvider, NbaseArcConstants{

    @Override
    public void setUp(TypeSetupContext context) {
        context.addType(NBASE_ARC);
    }
}
