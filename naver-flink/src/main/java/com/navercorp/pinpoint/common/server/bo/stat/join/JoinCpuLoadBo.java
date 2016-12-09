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
package com.navercorp.pinpoint.common.server.bo.stat.join;

import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinCpuLoadBo {
    private String agentId;
    private double jvmCpuLoad = -1.0D;
    private double maxJvmCpuLoad = -1.0D;
    private double minJvmCpuLoad = -1.0D;
    private double systemCpuLoad = -1.0D;
    private double maxSystemCpuLoad = -1.0D;;
    private double minSystemCpuLoad = -1.0D;;
    private long timestamp;

    public void setJvmCpuLoad(double jvmCpuLoad) {
        this.jvmCpuLoad = jvmCpuLoad;
    }

    public void setSystemCpuLoad(double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public double getJvmCpuLoad() {
        return jvmCpuLoad;
    }

    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public String getAgentId() {
        return agentId;
    }

    public static JoinCpuLoadBo joinCpuLoadBoList(List<JoinCpuLoadBo> joinCpuLoadBoList) {
        JoinCpuLoadBo newJoinCpuLoadBo = new JoinCpuLoadBo();
        int boCount = joinCpuLoadBoList.size();

        if (boCount == 0) {
            return newJoinCpuLoadBo;
        }
        JoinCpuLoadBo initCpuLoadBo = joinCpuLoadBoList.get(0);
        newJoinCpuLoadBo.setAgentId(initCpuLoadBo.getAgentId());
        newJoinCpuLoadBo.setTimestamp(initCpuLoadBo.getTimestamp());

        double sumJvmCpuLoad = 0D;
        double jvmCpuLoad = initCpuLoadBo.getJvmCpuLoad();
        double maxJvmCpuLoad = initCpuLoadBo.getMaxJvmCpuLoad();
        double minJvmCpuLoad = initCpuLoadBo.getMinJvmCpuLoad();
        double sumSystemCpuLoad = 0D;
        double systemCpuLoad = initCpuLoadBo.getSystemCpuLoad();
        double maxSystemCpuLoad = initCpuLoadBo.getMaxSystemCpuLoad();
        double minSystemCpuLoad = initCpuLoadBo.getMinSystemCpuLoad();

        for (JoinCpuLoadBo joinCpuLoadBo : joinCpuLoadBoList) {
            sumJvmCpuLoad += joinCpuLoadBo.getJvmCpuLoad();
            if (joinCpuLoadBo.getMaxJvmCpuLoad() > maxJvmCpuLoad) {
                maxJvmCpuLoad = joinCpuLoadBo.getMaxJvmCpuLoad();
            }
            if (joinCpuLoadBo.getMinJvmCpuLoad() < minJvmCpuLoad) {
                minJvmCpuLoad = joinCpuLoadBo.getMinJvmCpuLoad();
            }

            sumSystemCpuLoad += joinCpuLoadBo.getSystemCpuLoad();
            if (joinCpuLoadBo.getMaxSystemCpuLoad() > maxSystemCpuLoad) {
                maxSystemCpuLoad = joinCpuLoadBo.getMaxSystemCpuLoad();
            }
            if (joinCpuLoadBo.getMinSystemCpuLoad() < minSystemCpuLoad) {
                minSystemCpuLoad = joinCpuLoadBo.getMinSystemCpuLoad();
            }
        }

        newJoinCpuLoadBo.setJvmCpuLoad(jvmCpuLoad / (double) boCount);
        newJoinCpuLoadBo.setMaxJvmCpuLoad(maxJvmCpuLoad);
        newJoinCpuLoadBo.setMinJvmCpuLoad(minJvmCpuLoad);
        newJoinCpuLoadBo.setSystemCpuLoad(systemCpuLoad / (double) boCount);
        newJoinCpuLoadBo.setMaxSystemCpuLoad(maxSystemCpuLoad);

        return newJoinCpuLoadBo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static JoinCpuLoadBo joinCpuLoadBoLIst(List<CpuLoadBo> cpuLoadBoList) {
        JoinCpuLoadBo joinCpuLoadBo = new JoinCpuLoadBo();
        int boCount = cpuLoadBoList.size();

        if (boCount == 0) {
            return joinCpuLoadBo;
        }
        CpuLoadBo initCpuLoadBo = cpuLoadBoList.get(0);
        joinCpuLoadBo.setAgentId(initCpuLoadBo.getAgentId());
        joinCpuLoadBo.setTimestamp(initCpuLoadBo.getTimestamp());
        double sumJvmCpuLoad = 0D;
        double jvmCpuLoad = initCpuLoadBo.getJvmCpuLoad();
        double maxJvmCpuLoad = jvmCpuLoad;
        double minJvmCpuLoad = jvmCpuLoad;
        double sumSystemCpuLoad = 0D;
        double systemCpuLoad = initCpuLoadBo.getSystemCpuLoad();
        double maxSystemCpuLoad = systemCpuLoad;
        double minSystemCpuLoad = systemCpuLoad;

        for (CpuLoadBo cpuLoadBo : cpuLoadBoList) {
            jvmCpuLoad = cpuLoadBo.getJvmCpuLoad();
            sumJvmCpuLoad += jvmCpuLoad;
            if (jvmCpuLoad > maxJvmCpuLoad) {
                maxJvmCpuLoad = jvmCpuLoad;
            }
            if (jvmCpuLoad < minJvmCpuLoad) {
                minJvmCpuLoad = jvmCpuLoad;
            }

            systemCpuLoad = cpuLoadBo.getSystemCpuLoad();
            sumSystemCpuLoad += systemCpuLoad;
            if (systemCpuLoad > maxSystemCpuLoad) {
                maxSystemCpuLoad = systemCpuLoad;
            }
            if (systemCpuLoad < minSystemCpuLoad) {
                minSystemCpuLoad = systemCpuLoad;
            }
        }

        joinCpuLoadBo.setJvmCpuLoad(sumJvmCpuLoad / (double) boCount);
        joinCpuLoadBo.setMaxJvmCpuLoad(maxJvmCpuLoad);
        joinCpuLoadBo.setMinJvmCpuLoad(minJvmCpuLoad);
        joinCpuLoadBo.setSystemCpuLoad(sumSystemCpuLoad / (double) boCount);
        joinCpuLoadBo.setMaxSystemCpuLoad(maxSystemCpuLoad);
        joinCpuLoadBo.setMinSystemCpuLoad(minSystemCpuLoad);

        return joinCpuLoadBo;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMaxJvmCpuLoad(double maxJvmCpuLoad) {
        this.maxJvmCpuLoad = maxJvmCpuLoad;
    }

    public void setMinJvmCpuLoad(double minJvmCpuLoad) {
        this.minJvmCpuLoad = minJvmCpuLoad;
    }

    public void setMaxSystemCpuLoad(double maxSystemCpuLoad) {
        this.maxSystemCpuLoad = maxSystemCpuLoad;
    }

    public void setMinSystemCpuLoad(double minSystemCpuLoad) {
        this.minSystemCpuLoad = minSystemCpuLoad;
    }

    public double getMaxJvmCpuLoad() {
        return maxJvmCpuLoad;
    }

    public double getMinJvmCpuLoad() {
        return minJvmCpuLoad;
    }

    public double getMaxSystemCpuLoad() {
        return maxSystemCpuLoad;
    }

    public double getMinSystemCpuLoad() {
        return minSystemCpuLoad;
    }
}
