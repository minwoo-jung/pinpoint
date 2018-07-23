/**
 * Copyright 2018 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.jeus;

import com.navercorp.pinpoint.bootstrap.config.ExcludeMethodFilter;
import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;
import com.navercorp.pinpoint.common.util.Assert;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class JeusConfig {

    private final boolean enable;

    private final boolean traceRequestParam;
    private final List<String> bootstrapMains;
    private final String realIpHeader;
    private final String realIpEmptyValue;

    private final Filter<String> excludeUrlFilter;
    private final Filter<String> excludeProfileMethodFilter;
    private final boolean hidePinpointHeader;

    public JeusConfig(ProfilerConfig config) {
        Assert.requireNonNull(config, "config must not be null");

        // plugin
        this.enable = config.readBoolean("profiler.jeus.enable", true);
        this.bootstrapMains = config.readList("profiler.jeus.bootstrap.main");
        // runtime
        this.traceRequestParam = config.readBoolean("profiler.jeus.tracerequestparam", true);
        this.realIpHeader = config.readString("profiler.jeus.realipheader", null);
        this.realIpEmptyValue = config.readString("profiler.jeus.realipemptyvalue", null);
        final String excludeURL = config.readString("profiler.jeus.excludeurl", "");
        if (!excludeURL.isEmpty()) {
            this.excludeUrlFilter = new ExcludePathFilter(excludeURL);
        } else {
            this.excludeUrlFilter = new SkipFilter<String>();
        }
        final String excludeProfileMethod = config.readString("profiler.jeus.excludemethod", "");
        if (!excludeProfileMethod.isEmpty()) {
            this.excludeProfileMethodFilter = new ExcludeMethodFilter(excludeProfileMethod);
        } else {
            this.excludeProfileMethodFilter = new SkipFilter<String>();
        }
        this.hidePinpointHeader = config.readBoolean("profiler.jeus.hidepinpointheader", true);
    }

    public boolean isEnable() {
        return enable;
    }

    public List<String> getBootstrapMains() {
        return bootstrapMains;
    }

    public boolean isTraceRequestParam() {
        return traceRequestParam;
    }

    public String getRealIpHeader() {
        return realIpHeader;
    }

    public String getRealIpEmptyValue() {
        return realIpEmptyValue;
    }

    public Filter<String> getExcludeProfileMethodFilter() {
        return excludeProfileMethodFilter;
    }

    public Filter<String> getExcludeUrlFilter() {
        return excludeUrlFilter;
    }

    public boolean isHidePinpointHeader() {
        return hidePinpointHeader;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JeusConfig{");
        sb.append("enable=").append(enable);
        sb.append(", traceRequestParam=").append(traceRequestParam);
        sb.append(", bootstrapMains=").append(bootstrapMains);
        sb.append(", realIpHeader='").append(realIpHeader).append('\'');
        sb.append(", realIpEmptyValue='").append(realIpEmptyValue).append('\'');
        sb.append(", excludeUrlFilter=").append(excludeUrlFilter);
        sb.append(", excludeProfileMethodFilter=").append(excludeProfileMethodFilter);
        sb.append(", hidePinpointHeader=").append(hidePinpointHeader);
        sb.append('}');
        return sb.toString();
    }
}
