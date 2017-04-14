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
package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.join;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSamplingHandler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;

import java.util.*;

/**
 * @author minwoo.jung
 */
//TODO : (minwoo) agent 이름 중복 이슈는 없으므로 굳이 소팅작업은 필요없을듯함. 개선 필요한 사항임.
public class EagerSamplingHandler <T extends JoinStatBo, S extends SampledAgentStatDataPoint> implements ApplicationStatSamplingHandler<T, S> {

    private final TimeWindow timeWindow;
    private final ApplicationStatSampler<T, S> sampler;

    private final Map<String, SamplingPartitionContext> samplingContexts = new HashMap<>();
    private final Map<Long, SortedMap<String, S>> sampledPointProjection = new TreeMap<>();

    public EagerSamplingHandler(TimeWindow timeWindow, ApplicationStatSampler<T, S> sampler) {
        this.timeWindow = timeWindow;
        this.sampler = sampler;
    }

    public void addDataPoint(T dataPoint) {
        String id = dataPoint.getId();
        long timestamp = dataPoint.getTimestamp();
        long timeslotTimestamp = timeWindow.refineTimestamp(timestamp);
        SamplingPartitionContext samplingContext = samplingContexts.get(id);
        if (samplingContext == null) {
            samplingContext = new SamplingPartitionContext(timeslotTimestamp, dataPoint);
            samplingContexts.put(id, samplingContext);
        } else {
            long timeslotTimestampToSample = samplingContext.getTimeslotTimestamp();
            if (timeslotTimestampToSample == timeslotTimestamp) {
                samplingContext.addDataPoint(dataPoint);
            } else if (timeslotTimestampToSample > timeslotTimestamp) {
                S sampledPoint = samplingContext.sampleDataPoints(dataPoint);
                SortedMap<String, S> sampledPoints = sampledPointProjection.get(timeslotTimestampToSample);
                if (sampledPoints == null) {
                    sampledPoints = new TreeMap<>();
                    sampledPointProjection.put(timeslotTimestampToSample, sampledPoints);
                }
                sampledPoints.put(id, sampledPoint);
                samplingContext = new SamplingPartitionContext(timeslotTimestamp, dataPoint);
                samplingContexts.put(id, samplingContext);
            } else {
                // Results should be sorted in a descending order of their actual timestamp values
                // as they are stored using reverse timestamp.
                throw new IllegalStateException("Out of order AgentStatDataPoint");
            }
        }
    }

    public List<S> getSampledDataPoints() {
        // sample remaining data point projections
        for (Map.Entry<String, SamplingPartitionContext> e : samplingContexts.entrySet()) {
            String id = e.getKey();
            SamplingPartitionContext samplingPartitionContext = e.getValue();
            long timeslotTimestamp = samplingPartitionContext.getTimeslotTimestamp();
            S sampledDataPoint = samplingPartitionContext.sampleDataPoints();
            SortedMap<String, S> reduceCandidates = sampledPointProjection.get(timeslotTimestamp);
            if (reduceCandidates == null) {
                reduceCandidates = new TreeMap<>();
                sampledPointProjection.put(timeslotTimestamp, reduceCandidates);
            }
            reduceCandidates.put(id, sampledDataPoint);
        }
        // reduce projection
        if (sampledPointProjection.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<S> sampledDataPoints = new ArrayList<>(sampledPointProjection.size());
            for (SortedMap<String, S> sampledPointCandidates : sampledPointProjection.values()) {
                sampledDataPoints.add(reduceSampledPoints(sampledPointCandidates));
            }
            return sampledDataPoints;
        }
    }

    private S reduceSampledPoints(SortedMap<String, S> sampledPointCandidates) {
        String lastKey = sampledPointCandidates.lastKey();
        return sampledPointCandidates.get(lastKey);
    }

    private class SamplingPartitionContext {

        private final int timeslotIndex;
        private final long timeslotTimestamp;
        private final List<T> dataPoints = new ArrayList<>();

        private SamplingPartitionContext(long timeslotTimestamp, T initialDataPoint) {
            this.timeslotTimestamp = timeslotTimestamp;
            this.dataPoints.add(initialDataPoint);
            this.timeslotIndex = timeWindow.getWindowIndex(this.timeslotTimestamp);
        }

        private void addDataPoint(T dataPoint) {
            this.dataPoints.add(dataPoint);
        }

        private long getTimeslotTimestamp() {
            return timeslotTimestamp;
        }

        private S sampleDataPoints() {
            return sampleDataPoints(null);
        }

        private S sampleDataPoints(T previousDataPoint) {
            return sampler.sampleDataPoints(timeslotIndex, timeslotTimestamp, dataPoints, previousDataPoint);
        }

    }
}