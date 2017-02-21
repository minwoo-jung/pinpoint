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

package pinpoint.common.server.bo.codec.stat.join;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.join.CpuLoadCodec;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author minwoo.jung
 */
public class CpuLoadCodecTest {

    @Test
    public void encodeValues(){
        final String id = "test_server";
        final long currentTime = new Date().getTime();
        final AgentStatDataPointCodec agentStatDataPointCodec = new AgentStatDataPointCodec();
        final CpuLoadCodec cpuLoadCodec = new CpuLoadCodec(agentStatDataPointCodec);
        final Buffer encodedValueBuffer = new AutomaticBuffer();
        final List<JoinCpuLoadBo> joinCpuLoadBoList = createJoinCpuLoadBoList(currentTime);
        encodedValueBuffer.putByte(cpuLoadCodec.getVersion());
        cpuLoadCodec.encodeValues(encodedValueBuffer, joinCpuLoadBoList);

        final Buffer valueBuffer = new FixedBuffer(encodedValueBuffer.getBuffer());;
        final long baseTimestamp = AgentStatUtils.getBaseTimestamp(currentTime);
        final long timestampDelta = currentTime - baseTimestamp;
        final AgentStatDecodingContext decodingContext = new AgentStatDecodingContext();
        decodingContext.setAgentId(id);
        decodingContext.setBaseTimestamp(baseTimestamp);
        decodingContext.setTimestampDelta(timestampDelta);

        assertEquals(valueBuffer.readByte(), cpuLoadCodec.getVersion());
        List<JoinCpuLoadBo> decodedjoinCpuLoadBoList = cpuLoadCodec.decodeValues(valueBuffer, decodingContext);
        for (int i = 0; i < decodedjoinCpuLoadBoList.size(); i++) {
            assertTrue(decodedjoinCpuLoadBoList.get(i).equals(joinCpuLoadBoList.get(i)));
        }
    }

    private List<JoinCpuLoadBo> createJoinCpuLoadBoList(long currentTime) {
        final String id = "test_server";
        final List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        JoinCpuLoadBo joinCpuLoadBo2 = new JoinCpuLoadBo(id, 40, 87, 40, 70, 97, 40, currentTime + 5000);
        JoinCpuLoadBo joinCpuLoadBo4 = new JoinCpuLoadBo(id, 20, 67, 17, 40, 99, 18, currentTime + 15000);
        JoinCpuLoadBo joinCpuLoadBo1 = new JoinCpuLoadBo(id, 50, 97, 27, 80, 97, 46, currentTime);
        JoinCpuLoadBo joinCpuLoadBo3 = new JoinCpuLoadBo(id, 30, 77, 27, 60, 77, 27, currentTime + 10000);
        JoinCpuLoadBo joinCpuLoadBo5 = new JoinCpuLoadBo(id, 10, 99, 7, 30, 59, 8, currentTime + 20000);
        joinCpuLoadBoList.add(joinCpuLoadBo1);
        joinCpuLoadBoList.add(joinCpuLoadBo2);
        joinCpuLoadBoList.add(joinCpuLoadBo3);
        joinCpuLoadBoList.add(joinCpuLoadBo4);
        joinCpuLoadBoList.add(joinCpuLoadBo5);
        return joinCpuLoadBoList;
    }

}