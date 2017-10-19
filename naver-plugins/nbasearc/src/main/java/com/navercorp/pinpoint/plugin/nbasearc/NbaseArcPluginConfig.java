/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.nbasearc;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author jaehong.kim
 */
public class NbaseArcPluginConfig {
    private final boolean enable;
    private final boolean pipeline;
    private final boolean io;

    public NbaseArcPluginConfig(ProfilerConfig src) {
        if (src.readBoolean("profiler.nbase_arc", true)) {
            this.enable = src.readBoolean("profiler.nbase_arc.enable", true);
        } else {
            this.enable = false;
        }
        this.pipeline = src.readBoolean("profiler.nbase_arc.pipeline", true);
        this.io = src.readBoolean("profiler.nbase_arc.io", true);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isPipeline() {
        return pipeline;
    }

    public boolean isIo() {
        return io;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{enable=");
        builder.append(enable);
        builder.append(", pipeline=");
        builder.append(pipeline);
        builder.append(", io=");
        builder.append(io);
        builder.append("}");
        return builder.toString();
    }
}
