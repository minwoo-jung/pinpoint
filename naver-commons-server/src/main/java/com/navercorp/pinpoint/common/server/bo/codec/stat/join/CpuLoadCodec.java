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
package com.navercorp.pinpoint.common.server.bo.codec.stat.join;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class CpuLoadCodec implements ApplicationStatCodec<JoinCpuLoadBo> {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public CpuLoadCodec(AgentStatDataPointCodec codec) {
        Assert.notNull(codec, "agentStatDataPointCodec must not be null");
        this.codec = codec;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinCpuLoadBo> joinCpuLoadBoList) {
        if (CollectionUtils.isEmpty(joinCpuLoadBoList)) {
            throw new IllegalArgumentException("cpuLoadBos must not be empty");
        }
        final int numValues = joinCpuLoadBoList.size();
        valueBuffer.putVInt(numValues);

        List<Long> timestamps = new ArrayList<Long>(numValues);
        UnsignedLongEncodingStrategy.Analyzer.Builder jvmCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minJvmCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxJvmCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder systemCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minSystemCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxSystemCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        for (JoinCpuLoadBo cpuLoadBo : joinCpuLoadBoList) {
            timestamps.add(cpuLoadBo.getTimestamp());
            jvmCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(cpuLoadBo.getJvmCpuLoad()));
            minJvmCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(cpuLoadBo.getMinJvmCpuLoad()));
            maxJvmCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(cpuLoadBo.getMaxJvmCpuLoad()));
            systemCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(cpuLoadBo.getSystemCpuLoad()));
            minSystemCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(cpuLoadBo.getMinSystemCpuLoad()));
            maxSystemCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(cpuLoadBo.getMaxSystemCpuLoad()));
        }
        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer, jvmCpuLoadAnalyzerBuilder.build(), minJvmCpuLoadAnalyzerBuilder.build(), maxJvmCpuLoadAnalyzerBuilder.build(), systemCpuLoadAnalyzerBuilder.build(), minSystemCpuLoadAnalyzerBuilder.build(), maxSystemCpuLoadAnalyzerBuilder.build());
    }

    private void encodeDataPoints(Buffer valueBuffer,
                    StrategyAnalyzer<Long> jvmCpuLoadStrategyAnalyzer,
                    StrategyAnalyzer<Long> minJvmCpuLoadStrategyAnalyzer,
                    StrategyAnalyzer<Long> maxJvmCpuLoadStrategyAnalyzer,
                    StrategyAnalyzer<Long> systemCpuLoadStrategyAnalyzer,
                    StrategyAnalyzer<Long> minSystemCpuLoadStrategyAnalyzer,
                    StrategyAnalyzer<Long> maxSystemCpuLoadStrategyAnalyzer) {
        // encode header
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(jvmCpuLoadStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minJvmCpuLoadStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxJvmCpuLoadStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(systemCpuLoadStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minSystemCpuLoadStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxSystemCpuLoadStrategyAnalyzer.getBestStrategy().getCode());
        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);
        // encode values
        this.codec.encodeValues(valueBuffer, jvmCpuLoadStrategyAnalyzer.getBestStrategy(), jvmCpuLoadStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minJvmCpuLoadStrategyAnalyzer.getBestStrategy(), minJvmCpuLoadStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxJvmCpuLoadStrategyAnalyzer.getBestStrategy(), maxJvmCpuLoadStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, systemCpuLoadStrategyAnalyzer.getBestStrategy(), systemCpuLoadStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minSystemCpuLoadStrategyAnalyzer.getBestStrategy(), minSystemCpuLoadStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxSystemCpuLoadStrategyAnalyzer.getBestStrategy(), maxSystemCpuLoadStrategyAnalyzer.getValues());
    }

    @Override
    public List<JoinCpuLoadBo> decodeValues(Buffer valueBuffer, ApplicationStatDecodingContext decodingContext) {
        final String id = decodingContext.getApplicationId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        int numValues = valueBuffer.readVInt();
        List<Long> timestamps = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        // decode headers
        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);
        EncodingStrategy<Long> jvmCpuLoadEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> minJvmCpuLoadEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> maxJvmCpuLoadEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> systemCpuLoadEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> minSystemCpuLoadEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> maxSystemCpuLoadEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());

        // decode values
        List<Long> jvmCpuLoads = this.codec.decodeValues(valueBuffer, jvmCpuLoadEncodingStrategy, numValues);
        List<Long> minJvmCpuLoads = this.codec.decodeValues(valueBuffer, minJvmCpuLoadEncodingStrategy, numValues);
        List<Long> maxJvmCpuLoads = this.codec.decodeValues(valueBuffer, maxJvmCpuLoadEncodingStrategy, numValues);
        List<Long> systemCpuLoads = this.codec.decodeValues(valueBuffer, systemCpuLoadEncodingStrategy, numValues);
        List<Long> minSystemCpuLoads = this.codec.decodeValues(valueBuffer, minSystemCpuLoadEncodingStrategy, numValues);
        List<Long> maxSystemCpuLoads = this.codec.decodeValues(valueBuffer, maxSystemCpuLoadEncodingStrategy, numValues);

        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>(numValues);
        for (int i = 0; i < numValues; ++i) {
            JoinCpuLoadBo joinCpuLoadBo = new JoinCpuLoadBo();
            joinCpuLoadBo.setId(id);
            joinCpuLoadBo.setTimestamp(timestamps.get(i));
            joinCpuLoadBo.setJvmCpuLoad(AgentStatUtils.convertLongToDouble(jvmCpuLoads.get(i)));
            joinCpuLoadBo.setMinJvmCpuLoad(AgentStatUtils.convertLongToDouble(minJvmCpuLoads.get(i)));
            joinCpuLoadBo.setMaxJvmCpuLoad(AgentStatUtils.convertLongToDouble(maxJvmCpuLoads.get(i)));
            joinCpuLoadBo.setSystemCpuLoad(AgentStatUtils.convertLongToDouble(systemCpuLoads.get(i)));
            joinCpuLoadBo.setMinSystemCpuLoad(AgentStatUtils.convertLongToDouble(minSystemCpuLoads.get(i)));
            joinCpuLoadBo.setMaxSystemCpuLoad(AgentStatUtils.convertLongToDouble(maxSystemCpuLoads.get(i)));
            joinCpuLoadBoList.add(joinCpuLoadBo);
        }
        return joinCpuLoadBoList;
    }
}
