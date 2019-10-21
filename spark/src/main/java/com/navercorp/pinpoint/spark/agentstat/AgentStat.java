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

package com.navercorp.pinpoint.spark.agentstat;

import java.io.Serializable;
import java.util.Map;

import com.navercorp.pinpoint.common.trace.SlotType;

/**
 * @author HyunGil Jeong
 */
public class AgentStat implements Serializable {

    private static final long serialVersionUID = 4473997453389991082L;

    public static final int NOT_COLLECTED = -1;

    private final String agentId;
    private final long timestamp;

    private long collectInterval;

    private String gcType;
    private long gcOldCount = NOT_COLLECTED;
    private long gcOldTime = NOT_COLLECTED;
    private long heapUsed = NOT_COLLECTED;
    private long heapMax = NOT_COLLECTED;
    private long nonHeapUsed = NOT_COLLECTED;
    private long nonHeapMax = NOT_COLLECTED;

    private double jvmCpuUsage = NOT_COLLECTED;
    private double systemCpuUsage = NOT_COLLECTED;

    private long sampledNewCount = NOT_COLLECTED;
    private long sampledContinuationCount = NOT_COLLECTED;
    private long unsampledNewCount = NOT_COLLECTED;
    private long unsampledContinuationCount = NOT_COLLECTED;

    private int histogramSchema;
    private Map<SlotType, Integer> activeTraceCounts;

    public AgentStat(String agentId, long timestamp) {
        if (agentId == null) {
            throw new NullPointerException("agentId");
        }
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be negative");
        }
        this.agentId = agentId;
        this.timestamp = timestamp;
    }

    public String getAgentId() {
        return this.agentId;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getCollectInterval() {
        return this.collectInterval;
    }

    public void setCollectInterval(long collectInterval) {
        this.collectInterval = collectInterval;
    }

    public String getGcType() {
        return gcType;
    }

    public void setGcType(String gcType) {
        this.gcType = gcType;
    }

    public long getGcOldCount() {
        return gcOldCount;
    }

    public void setGcOldCount(long gcOldCount) {
        this.gcOldCount = gcOldCount;
    }

    public long getGcOldTime() {
        return gcOldTime;
    }

    public void setGcOldTime(long gcOldTime) {
        this.gcOldTime = gcOldTime;
    }

    public long getHeapUsed() {
        return heapUsed;
    }

    public void setHeapUsed(long heapUsed) {
        this.heapUsed = heapUsed;
    }

    public long getHeapMax() {
        return heapMax;
    }

    public void setHeapMax(long heapMax) {
        this.heapMax = heapMax;
    }

    public long getNonHeapUsed() {
        return nonHeapUsed;
    }

    public void setNonHeapUsed(long nonHeapUsed) {
        this.nonHeapUsed = nonHeapUsed;
    }

    public long getNonHeapMax() {
        return nonHeapMax;
    }

    public void setNonHeapMax(long nonHeapMax) {
        this.nonHeapMax = nonHeapMax;
    }

    public double getJvmCpuUsage() {
        return jvmCpuUsage;
    }

    public void setJvmCpuUsage(double jvmCpuUsage) {
        this.jvmCpuUsage = jvmCpuUsage;
    }

    public double getSystemCpuUsage() {
        return systemCpuUsage;
    }

    public void setSystemCpuUsage(double systemCpuUsage) {
        this.systemCpuUsage = systemCpuUsage;
    }

    public long getSampledNewCount() {
        return sampledNewCount;
    }

    public void setSampledNewCount(long sampledNewCount) {
        this.sampledNewCount = sampledNewCount;
    }

    public long getSampledContinuationCount() {
        return sampledContinuationCount;
    }

    public void setSampledContinuationCount(long sampledContinuationCount) {
        this.sampledContinuationCount = sampledContinuationCount;
    }

    public long getUnsampledNewCount() {
        return unsampledNewCount;
    }

    public void setUnsampledNewCount(long unsampledNewCount) {
        this.unsampledNewCount = unsampledNewCount;
    }

    public long getUnsampledContinuationCount() {
        return unsampledContinuationCount;
    }

    public void setUnsampledContinuationCount(long unsampledContinuationCount) {
        this.unsampledContinuationCount = unsampledContinuationCount;
    }

    public int getHistogramSchema() {
        return histogramSchema;
    }

    public void setHistogramSchema(int histogramSchema) {
        this.histogramSchema = histogramSchema;
    }

    public Map<SlotType, Integer> getActiveTraceCounts() {
        return activeTraceCounts;
    }

    public void setActiveTraceCounts(Map<SlotType, Integer> activeTraceCounts) {
        this.activeTraceCounts = activeTraceCounts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AgentStat [");
        sb.append("agentId=" + agentId + ", timestamp=" + timestamp + ", collectInterval=" + collectInterval);
        sb.append(", gcType=" + gcType + ", gcOldCount=" + gcOldCount + ", gcOldTime=" + gcOldTime);
        sb.append(", heapUsed=" + heapUsed + ", heapMax=" + heapMax + ", nonHeapUsed=" + nonHeapUsed);
        sb.append(", nonHeapMax=" + nonHeapMax + ", jvmCpuUsage=" + jvmCpuUsage + ", systemCpuUsage=" + systemCpuUsage);
        sb.append(", sampledNewCount=" + sampledNewCount + ", sampledContinuationCount=" + sampledContinuationCount);
        sb.append(", unsampledNewCount=" + unsampledNewCount + ", unsampledContinuationCount=" + unsampledContinuationCount);
        sb.append(", histogramSchemaTypeCode=" + histogramSchema);

        if (activeTraceCounts != null) {
            sb.append(", activeTraceCounts=" + activeTraceCounts);
        }
        
        sb.append("]");
        
        return sb.toString();
    }

    public static AgentStat add(AgentStat s1, AgentStat s2) {
        AgentStat latest = s1.getTimestamp() > s2.getTimestamp() ? s1 : s2;

        AgentStat stat = new AgentStat(s1.getAgentId(), s1.getTimestamp());
        
        stat.setGcType(latest.getGcType());
        stat.setGcOldCount(latest.getGcOldCount());
        stat.setGcOldTime(latest.getGcOldTime());
        
        stat.setHeapUsed(latest.getHeapUsed());
        stat.setHeapMax(maxValue(s1.getHeapMax(), s2.getHeapMax()));
        
        stat.setNonHeapUsed(latest.getNonHeapUsed());
        stat.setNonHeapMax(maxValue(s1.getNonHeapMax(), s2.getNonHeapMax()));
        
        stat.setJvmCpuUsage(latest.getJvmCpuUsage());
        stat.setSystemCpuUsage(latest.getSystemCpuUsage());
        
        stat.setSampledNewCount(addValue(s1.getSampledNewCount(), s2.getSampledNewCount()));
        stat.setSampledContinuationCount(addValue(s1.getSampledContinuationCount(), s2.getSampledContinuationCount()));
        stat.setUnsampledNewCount(addValue(s1.getUnsampledNewCount(), s2.getUnsampledNewCount()));
        stat.setUnsampledContinuationCount(addValue(s1.getUnsampledContinuationCount(), s2.getUnsampledContinuationCount()));
        
        stat.setHistogramSchema(latest.getHistogramSchema());
        stat.setActiveTraceCounts(latest.getActiveTraceCounts());
        
        return stat;
    }
    
    private static long addValue(long v1, long v2) {
        if (v1 == NOT_COLLECTED) {
            if (v2 == NOT_COLLECTED) {
                return NOT_COLLECTED;
            } else {
                return v2;
            }
        } else {
            if (v1 == NOT_COLLECTED) {
                return v1;
            } else {
                return v1 + v2;
            }
        }
    }
    
    private static long maxValue(long v1, long v2) {
        if (v1 == NOT_COLLECTED) {
            return v2;
        } else if (v2 == NOT_COLLECTED) {
            return v1;
        }
        
        return v1 < v2 ? v2 : v1;
    }
}
