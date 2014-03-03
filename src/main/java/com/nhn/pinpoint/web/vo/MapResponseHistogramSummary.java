package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.util.TimeWindow;
import com.nhn.pinpoint.web.util.TimeWindowOneMinuteSampler;

import javax.xml.ws.Response;
import java.util.*;

/**
 * @author emeroad
 */
public class MapResponseHistogramSummary {

    private final Range range;
    private final TimeWindow window;

    private Map<Long, Map<Application, ResponseTime>> responseTimeApplicationMap = new HashMap<Long, Map<Application, ResponseTime>>();
    private Map<Application, List<ResponseTime>> result = new HashMap<Application, List<ResponseTime>>();


    public MapResponseHistogramSummary(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.range = range;
        // 일단 샘플링을 하지 않도록한다.
        this.window = new TimeWindow(range, TimeWindowOneMinuteSampler.SAMPLER);

    }

    public void addHistogram(Application application, SpanBo span, long timeStamp) {
        timeStamp = window.refineTimestamp(timeStamp);


        final ResponseTime responseTime = getResponseTime(application, timeStamp);
        if (span.getErrCode() != 0) {
            responseTime.addResponseTime(span.getAgentId(), HistogramSchema.ERROR_SLOT_TIME);
        } else {
            responseTime.addResponseTime(span.getAgentId(), span.getElapsed());
        }

    }

    private ResponseTime getResponseTime(Application application, Long timeStamp) {
        Map<Application, ResponseTime> responseTimeMap = responseTimeApplicationMap.get(timeStamp);
        if (responseTimeMap == null) {
            responseTimeMap = new HashMap<Application, ResponseTime>();
            responseTimeApplicationMap.put(timeStamp, responseTimeMap);
        }
        ResponseTime responseTime = responseTimeMap.get(application);
        if (responseTime == null) {
            responseTime = new ResponseTime(application.getName(), application.getServiceTypeCode(), timeStamp);
            responseTimeMap.put(application, responseTime);
        }
        return responseTime;
    }

    public void build() {
        final Map<Application, List<ResponseTime>> result = new HashMap<Application, List<ResponseTime>>();

        for (Map<Application, ResponseTime> entry : responseTimeApplicationMap.values()) {
            for (Map.Entry<Application, ResponseTime> applicationResponseTimeEntry : entry.entrySet()) {
                List<ResponseTime> responseTimeList = result.get(applicationResponseTimeEntry.getKey());
                if (responseTimeList == null) {
                    responseTimeList = new ArrayList<ResponseTime>();
                    result.put(applicationResponseTimeEntry.getKey(), responseTimeList);
                }
                responseTimeList.add(applicationResponseTimeEntry.getValue());
            }
        }

        this.responseTimeApplicationMap = null;
        this.result = result;

    }

    public List<ResponseTime> getResponseTimeList(Application application) {
        return this.result.get(application);
    }
}
