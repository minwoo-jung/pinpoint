package com.navercorp.pinpoint.plugin.lucy.net.npc;

import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

/**
 * @author Taejin Koo
 */
public abstract class NpcPlugin {

    private static final PLogger LOGGER = PLoggerFactory.getLogger(NpcPlugin.class);
    final ProfilerPluginSetupContext context;
    final TransformTemplate transformTemplate;

    public NpcPlugin(ProfilerPluginSetupContext context, TransformTemplate transformTemplate) {
        this.context = context;
        this.transformTemplate = transformTemplate;
    }

    abstract String getEditClazzName();
    abstract void transform();

}
