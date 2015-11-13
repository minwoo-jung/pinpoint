package com.navercorp.pinpoint.plugin.lucy.net.npc;

import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

/**
 * @author Taejin Koo
 */
public class NpcPluginHolder {

    private final ProfilerPluginSetupContext context;
    private final TransformTemplate transformTemplate;
    

    public NpcPluginHolder(ProfilerPluginSetupContext context, TransformTemplate transformTemplate) {
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        if (transformTemplate == null) {
            throw new NullPointerException("transformTemplate must not be null");
        }
        this.context = context;
        this.transformTemplate = transformTemplate;
    }

    public void addPlugin() {
        NpcHessianConnectorPlugin npcPlugin = new NpcHessianConnectorPlugin(context, transformTemplate);
        npcPlugin.transform();
        
        NioPlugin nioPlugin = new NioPlugin(context, transformTemplate);
        nioPlugin.transform();
        
        LightWeightPlugin lightWeightPlugin = new LightWeightPlugin(context, transformTemplate);
        lightWeightPlugin.transform();

        LightWeightNpcHessianPlugin lightWeightNpcHessianPlugin = new LightWeightNpcHessianPlugin(context, transformTemplate);
        lightWeightNpcHessianPlugin.transform();

        LegacyLightWeightPlugin legacyLightWeightPlugin = new LegacyLightWeightPlugin(context, transformTemplate);
        legacyLightWeightPlugin.transform();

        KeepAlivePlugin keepAlivePlugin = new KeepAlivePlugin(context, transformTemplate);
        keepAlivePlugin.transform();
    }


}
