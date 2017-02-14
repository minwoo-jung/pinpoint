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

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinCpuLoadBo implements JoinStatBo {
    public static final double UNCOLLECTED_VALUE = -1;

    private final byte version = 1;
    private String id;
    //TODO : (minwoo) 명시적으로 average를 prefix로 써야하는건 아닌지?
    private double jvmCpuLoad = -1.0D;
    private double maxJvmCpuLoad = -1.0D;
    private double minJvmCpuLoad = -1.0D;
    private double systemCpuLoad = -1.0D;
    private double maxSystemCpuLoad = -1.0D;;
    private double minSystemCpuLoad = -1.0D;;
    private long timestamp;

    public JoinCpuLoadBo() {
    }

    protected JoinCpuLoadBo(String id, double jvmCpuLoad, double maxJvmCpuLoad, double minJvmCpuLoad, double systemCpuLoad, double maxSystemCpuLoad, double minSystemCpuLoad, long timestamp) {
        this.id = id;
        this.jvmCpuLoad = jvmCpuLoad;
        this.maxJvmCpuLoad = maxJvmCpuLoad;
        this.minJvmCpuLoad = minJvmCpuLoad;
        this.systemCpuLoad = systemCpuLoad;
        this.maxSystemCpuLoad = maxSystemCpuLoad;
        this.minSystemCpuLoad = minSystemCpuLoad;
        this.timestamp = timestamp;
    }

    public void setJvmCpuLoad(double jvmCpuLoad) {
        this.jvmCpuLoad = jvmCpuLoad;
    }

    public void setSystemCpuLoad(double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getJvmCpuLoad() {
        return jvmCpuLoad;
    }

    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public String getId() {
        return id;
    }

    public static JoinCpuLoadBo joinCpuLoadBoList(List<JoinCpuLoadBo> joinCpuLoadBoList, Long timestamp) {
        JoinCpuLoadBo newJoinCpuLoadBo = new JoinCpuLoadBo();
        int boCount = joinCpuLoadBoList.size();

        if (boCount == 0) {
            return newJoinCpuLoadBo;
        }
        JoinCpuLoadBo initCpuLoadBo = joinCpuLoadBoList.get(0);
        newJoinCpuLoadBo.setId(initCpuLoadBo.getId());
        newJoinCpuLoadBo.setTimestamp(timestamp);

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

        newJoinCpuLoadBo.setJvmCpuLoad(sumJvmCpuLoad / (double) boCount);
        newJoinCpuLoadBo.setMaxJvmCpuLoad(maxJvmCpuLoad);
        newJoinCpuLoadBo.setMinJvmCpuLoad(minJvmCpuLoad);
        newJoinCpuLoadBo.setSystemCpuLoad(sumSystemCpuLoad / (double) boCount);
        newJoinCpuLoadBo.setMinSystemCpuLoad(minSystemCpuLoad);
        newJoinCpuLoadBo.setMaxSystemCpuLoad(maxSystemCpuLoad);

        return newJoinCpuLoadBo;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public static JoinCpuLoadBo joinCpuLoadBoListForCpuLoadBoList(List<CpuLoadBo> cpuLoadBoList) {
        JoinCpuLoadBo joinCpuLoadBo = new JoinCpuLoadBo();
        int boCount = cpuLoadBoList.size();

        if (boCount == 0) {
            return joinCpuLoadBo;
        }
        CpuLoadBo initCpuLoadBo = cpuLoadBoList.get(0);
        joinCpuLoadBo.setId(initCpuLoadBo.getAgentId());
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

    public static JoinCpuLoadBo convertJoinCpuLoadBo(CpuLoadBo cpuLoadBo) {
        JoinCpuLoadBo joinCpuLoadBo = new JoinCpuLoadBo();
        joinCpuLoadBo.setId(cpuLoadBo.getAgentId());
        joinCpuLoadBo.setTimestamp(cpuLoadBo.getTimestamp());
        joinCpuLoadBo.setJvmCpuLoad(cpuLoadBo.getJvmCpuLoad());
        joinCpuLoadBo.setMinJvmCpuLoad(cpuLoadBo.getJvmCpuLoad());
        joinCpuLoadBo.setMaxJvmCpuLoad(cpuLoadBo.getJvmCpuLoad());
        joinCpuLoadBo.setSystemCpuLoad(cpuLoadBo.getSystemCpuLoad());
        joinCpuLoadBo.setMinSystemCpuLoad(cpuLoadBo.getSystemCpuLoad());
        joinCpuLoadBo.setMaxSystemCpuLoad(cpuLoadBo.getSystemCpuLoad());
        return joinCpuLoadBo;
    }

    public static List<JoinCpuLoadBo> convertJoinCpuLoadBoList(List<CpuLoadBo> cpuLoadBos) {
        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        for (CpuLoadBo cpuLoadBo : cpuLoadBos) {
            joinCpuLoadBoList.add(convertJoinCpuLoadBo(cpuLoadBo));
        }
        return joinCpuLoadBoList;
    }

    public byte getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "JoinCpuLoadBo{" +
            "version=" + version +
            ", id='" + id + '\'' +
            ", jvmCpuLoad=" + jvmCpuLoad +
            ", maxJvmCpuLoad=" + maxJvmCpuLoad +
            ", minJvmCpuLoad=" + minJvmCpuLoad +
            ", systemCpuLoad=" + systemCpuLoad +
            ", maxSystemCpuLoad=" + maxSystemCpuLoad +
            ", minSystemCpuLoad=" + minSystemCpuLoad +
            ", timestamp=" + timestamp +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinCpuLoadBo that = (JoinCpuLoadBo) o;

        if (version != that.version) return false;
        if (Double.compare(that.jvmCpuLoad, jvmCpuLoad) != 0) return false;
        if (Double.compare(that.maxJvmCpuLoad, maxJvmCpuLoad) != 0) return false;
        if (Double.compare(that.minJvmCpuLoad, minJvmCpuLoad) != 0) return false;
        if (Double.compare(that.systemCpuLoad, systemCpuLoad) != 0) return false;
        if (Double.compare(that.maxSystemCpuLoad, maxSystemCpuLoad) != 0) return false;
        if (Double.compare(that.minSystemCpuLoad, minSystemCpuLoad) != 0) return false;
        if (timestamp != that.timestamp) return false;
        return id != null ? id.equals(that.id) : that.id == null;
    }

}
