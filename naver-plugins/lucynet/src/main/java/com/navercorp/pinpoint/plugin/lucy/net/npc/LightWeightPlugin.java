package com.navercorp.pinpoint.plugin.lucy.net.npc;

import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

/**
 * @author Taejin Koo
 */
class LightWeightPlugin extends LegacyLightWeightPlugin {

    public LightWeightPlugin(ProfilerPluginSetupContext context, TransformTemplate transformTemplate) {
        super(context, transformTemplate);
    }

    @Override
    public String getEditClazzName() {
        return "com.nhncorp.lucy.npc.connector.LightWeightConnector";
    }
    
}
