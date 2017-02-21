/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.UncollectedPoint;
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import org.apache.hadoop.hbase.shaded.com.google.common.collect.MinMaxPriorityQueue;
import org.apache.hadoop.hbase.shaded.org.apache.commons.math3.analysis.function.Min;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinCpuLoadSampler implements ApplicationStatSampler<JoinCpuLoadBo, SampledCpuLoad> {

    private static final int NUM_DECIMAL_PLACES = 1;
    public static final DownSampler<Double> DOUBLE_DOWN_SAMPLER = DownSamplers.getDoubleDownSampler(JoinCpuLoadBo.UNCOLLECTED_VALUE, NUM_DECIMAL_PLACES);

    @Override
    public SampledCpuLoad sampleDataPoints(int timeWindowIndex, long timestamp, List<JoinCpuLoadBo> dataPoints, JoinCpuLoadBo previousDataPoint) {
        List<Double> jvmCpuLoads = new ArrayList<>(dataPoints.size());
        List<Double> minJvmCpuLoads = new ArrayList<>(dataPoints.size());
        List<Double> maxJvmCpuLoads = new ArrayList<>(dataPoints.size());
        List<Double> systemCpuLoads = new ArrayList<>(dataPoints.size());
        List<Double> minSystemCpuLoads = new ArrayList<>(dataPoints.size());
        List<Double> maxSystemCpuLoads = new ArrayList<>(dataPoints.size());
        for (JoinCpuLoadBo cpuLoadBo : dataPoints) {
            if (cpuLoadBo.getJvmCpuLoad() != JoinCpuLoadBo.UNCOLLECTED_VALUE) {
                jvmCpuLoads.add(cpuLoadBo.getJvmCpuLoad() * 100);
                minJvmCpuLoads.add(cpuLoadBo.getMinJvmCpuLoad() * 100);
                maxJvmCpuLoads.add(cpuLoadBo.getMaxJvmCpuLoad() * 100);
            }
            if (cpuLoadBo.getSystemCpuLoad() != JoinCpuLoadBo.UNCOLLECTED_VALUE) {
                systemCpuLoads.add(cpuLoadBo.getSystemCpuLoad() * 100);
                minSystemCpuLoads.add(cpuLoadBo.getMinSystemCpuLoad() * 100);
                maxSystemCpuLoads.add(cpuLoadBo.getMaxSystemCpuLoad() * 100);
            }
        }
        SampledCpuLoad sampledCpuLoad = new SampledCpuLoad();
        sampledCpuLoad.setJvmCpuLoad(createPoint(timestamp, jvmCpuLoads, minJvmCpuLoads, maxJvmCpuLoads));
        sampledCpuLoad.setSystemCpuLoad(createPoint(timestamp, systemCpuLoads, minSystemCpuLoads, maxSystemCpuLoads));
        return sampledCpuLoad;
    }

    private Point<Long, Double> createPoint(long timestamp, List<Double> cpuLoads, List<Double> minCpuLoads, List<Double> maxCpuLoads) {
        if (cpuLoads.isEmpty() || minCpuLoads.isEmpty() || maxCpuLoads.isEmpty()) {
            return new UncollectedPoint<>(timestamp, JoinCpuLoadBo.UNCOLLECTED_VALUE);
        } else {
            return new Point<>(
                timestamp,
                DOUBLE_DOWN_SAMPLER.sampleMin(minCpuLoads),
                DOUBLE_DOWN_SAMPLER.sampleMax(maxCpuLoads),
                DOUBLE_DOWN_SAMPLER.sampleAvg(cpuLoads),
                DOUBLE_DOWN_SAMPLER.sampleSum(cpuLoads));
        }
    }
}
