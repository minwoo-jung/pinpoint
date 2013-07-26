package com.nhn.pinpoint.collector.dao;

/**
 * 
 * @author netspider
 * 
 */
public interface ApplicationMapStatisticsCalleeDao extends CachedStatisticsDao {
	void update(String callerApplicationName, short callerServiceType, String calleeApplicationName, short calleeServiceType, String calleeHost, int elapsed, boolean isError);
}
