/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.line;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author Jongho Moon
 *
 */
public class LineConfig {
    private final int paramDumpSize;
    private final int entityDumpSize;
    
    public LineConfig(ProfilerConfig config) {
        this.paramDumpSize = config.readInt("profiler.line.game.netty.param.dumpsize", 512);
        this.entityDumpSize = config.readInt("profiler.line.game.netty.entity.dumpsize", 512);
    }

    public int getParamDumpSize() {
        return paramDumpSize;
    }

    public int getEntityDumpSize() {
        return entityDumpSize;
    }
}
