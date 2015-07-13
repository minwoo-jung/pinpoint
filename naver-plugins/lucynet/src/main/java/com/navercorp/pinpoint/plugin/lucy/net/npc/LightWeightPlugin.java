package com.navercorp.pinpoint.plugin.lucy.net.npc;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;

/**
 * @author Taejin Koo
 */
class LightWeightPlugin extends LegacyLightWeightPlugin {

    public LightWeightPlugin(ProfilerPluginContext context) {
        super(context);
    }

    @Override
    public String getEditClazzName() {
        return "com.nhncorp.lucy.npc.connector.LightWeightConnector";
    }
    
}
