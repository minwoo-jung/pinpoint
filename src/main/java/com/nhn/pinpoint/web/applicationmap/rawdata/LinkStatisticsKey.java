package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.web.vo.Application;

/**
 * @author emeroad
 */
public class LinkStatisticsKey {

    private final Application fromApplication;
    private final Application toApplication;

    public LinkStatisticsKey(LinkStatistics linkStatistics) {
        this(linkStatistics.getFromApplication(), linkStatistics.getToApplication());
    }

    public LinkStatisticsKey(Application fromApplication, Application toApplication) {
        if (fromApplication == null) {
            throw new NullPointerException("fromApplication must not be null");
        }
        if (toApplication == null) {
            throw new NullPointerException("toApplication must not be null");
        }
        this.fromApplication = fromApplication;
        this.toApplication = toApplication;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkStatisticsKey that = (LinkStatisticsKey) o;

        if (!fromApplication.equals(that.fromApplication)) return false;
        if (!toApplication.equals(that.toApplication)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fromApplication.hashCode();
        result = 31 * result + toApplication.hashCode();
        return result;
    }
}
