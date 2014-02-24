package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;

import java.util.*;

/**
 * @author emeroad
 */
public class ResponseTime {
    // rowKey
    private final String applicationName;
    private final short applicationServiceType;
    private final long timeStamp;

    // agentId 이 key임.
    private final Map<String, Histogram> responseHistogramMap = new HashMap<String, Histogram>();


    public ResponseTime(String applicationName, short applicationServiceType, long timeStamp) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        this.applicationName = applicationName;
        this.applicationServiceType = applicationServiceType;
        this.timeStamp = timeStamp;
    }


    public String getApplicationName() {
        return applicationName;
    }

    public short getApplicationServiceType() {
        return applicationServiceType;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public Histogram getHistogram(String agentId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        final Histogram histogram = responseHistogramMap.get(agentId);
        if (histogram != null) {
            return histogram;
        }
        final Histogram newHistogram = new Histogram(applicationServiceType);
        responseHistogramMap.put(agentId, newHistogram);
        return newHistogram;
    }

    public void addResponseTime(String agentId, short slotNumber, long count) {
        getHistogram(agentId).addCallCount(slotNumber, count);
    }

    public Collection<Histogram> getResponseHistogramList() {
        return responseHistogramMap.values();
    }

    public Set<Map.Entry<String, Histogram>> getAgentHistogram() {
        return this.responseHistogramMap.entrySet();
    }

    @Override
    public String toString() {
        return "ResponseTime{" +
                "applicationName='" + applicationName + '\'' +
                ", applicationServiceType=" + applicationServiceType +
                ", timeStamp=" + timeStamp +
                ", responseHistogramMap=" + responseHistogramMap +
                '}';
    }

}
