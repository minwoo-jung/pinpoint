package com.navercorp.pinpoint.plugin.nbasearc;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class NbaseArcPluginConfig {

    private final boolean enabled;
    private final boolean pipelineEnabled;

    public NbaseArcPluginConfig(ProfilerConfig src) {
        enabled = src.readBoolean("profiler.nbase_arc", true);
        pipelineEnabled = src.readBoolean("profiler.nbase_arc.pipeline", true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPipelineEnabled() {
        return pipelineEnabled;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{enabled=");
        builder.append(enabled);
        builder.append(", pipelineEnabled=");
        builder.append(pipelineEnabled);
        builder.append("}");
        return builder.toString();
    }
}
