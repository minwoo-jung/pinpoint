/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jdbc.nbaset;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcConfig;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author jaehong.kim
 */
public class NbasetConfig extends JdbcConfig {
    private final boolean profileSetAutoCommit;
    private final boolean profileCommit;
    private final boolean profileRollback;
    private final boolean enable;

    public NbasetConfig(ProfilerConfig config) {
        super(config.readBoolean("profiler.jdbc.nbaset.tracesqlbindvalue", config.isTraceSqlBindValue()), config.getMaxSqlBindValueSize());
        this.profileSetAutoCommit = config.readBoolean("profiler.jdbc.nbaset.setautocommit", false);
        this.profileCommit = config.readBoolean("profiler.jdbc.nbaset.commit", false);
        this.profileRollback = config.readBoolean("profiler.jdbc.nbaset.rollback", false);
        this.enable = config.readBoolean("profiler.jdbc.nbaset", true);
    }

    public boolean isProfileSetAutoCommit() {
        return profileSetAutoCommit;
    }

    public boolean isProfileCommit() {
        return profileCommit;
    }

    public boolean isProfileRollback() {
        return profileRollback;
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NbasetConfig{");
        sb.append("profileSetAutoCommit=").append(profileSetAutoCommit);
        sb.append(", profileCommit=").append(profileCommit);
        sb.append(", profileRollback=").append(profileRollback);
        sb.append(", enable=").append(enable);
        sb.append('}');
        return sb.toString();
    }
}