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
package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class BlocPluginConfig {

    private final boolean blocEnable;
    private final List<String> blocBootstrapMains;
    private final boolean blocTraceRequestParam;

    public BlocPluginConfig(ProfilerConfig src) {
        this.blocEnable = src.readBoolean("profiler.bloc.enable", true);
        this.blocBootstrapMains = src.readList("profiler.bloc.bootstrap.main");
        this.blocTraceRequestParam = src.readBoolean("profiler.bloc.tracerequestparam", false);
    }

    public boolean isBlocEnable() {
        return blocEnable;
    }

    public List<String> getBlocBootstrapMains() {
        return blocBootstrapMains;
    }

    public boolean isBlocTraceRequestParam() {
        return blocTraceRequestParam;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BlocPluginConfig{");
        sb.append("blocEnable=").append(blocEnable);
        sb.append(", blocBootstrapMains=").append(blocBootstrapMains);
        sb.append(", blocTraceRequestParam=").append(blocTraceRequestParam);
        sb.append('}');
        return sb.toString();
    }
}