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
        npcPlugin.addRecipe();
        
        NioPlugin nioPlugin = new NioPlugin(context, transformTemplate);
        nioPlugin.addRecipe();
        
        LightWeightPlugin lightWeightPlugin = new LightWeightPlugin(context, transformTemplate);
        lightWeightPlugin.addRecipe();

        LightWeightNpcHessianPlugin lightWeightNpcHessianPlugin = new LightWeightNpcHessianPlugin(context, transformTemplate);
        lightWeightNpcHessianPlugin.addRecipe();

        LegacyLightWeightPlugin legacyLightWeightPlugin = new LegacyLightWeightPlugin(context, transformTemplate);
        legacyLightWeightPlugin.addRecipe();

        KeepAlivePlugin keepAlivePlugin = new KeepAlivePlugin(context, transformTemplate);
        keepAlivePlugin.addRecipe();
    }


}
