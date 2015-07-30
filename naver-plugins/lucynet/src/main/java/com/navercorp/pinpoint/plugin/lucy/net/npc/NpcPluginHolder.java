package com.navercorp.pinpoint.plugin.lucy.net.npc;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

/**
 * @author Taejin Koo
 */
public class NpcPluginHolder {

    private final ProfilerPluginSetupContext context;
    
    public NpcPluginHolder(ProfilerPluginSetupContext context) {
        this.context = context;
    }
    
    public void addPlugin() {
        NpcHessianConnectorPlugin npcPlugin = new NpcHessianConnectorPlugin(context);
        npcPlugin.addRecipe();
        
        NioPlugin nioPlugin = new NioPlugin(context);
        nioPlugin.addRecipe();
        
        LightWeightPlugin lightWeightPlugin = new LightWeightPlugin(context);
        lightWeightPlugin.addRecipe();

        LightWeightNpcHessianPlugin lightWeightNpcHessianPlugin = new LightWeightNpcHessianPlugin(context);
        lightWeightNpcHessianPlugin.addRecipe();

        LegacyLightWeightPlugin legacyLightWeightPlugin = new LegacyLightWeightPlugin(context);
        legacyLightWeightPlugin.addRecipe();

        KeepAlivePlugin keepAlivePlugin = new KeepAlivePlugin(context);
        keepAlivePlugin.addRecipe();
    }


}
