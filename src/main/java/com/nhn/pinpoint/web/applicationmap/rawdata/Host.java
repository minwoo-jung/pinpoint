package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.util.Mergeable;

/**
 * 
 * @author netspider
 * 
 */
public class Host implements Mergeable<Host> {
	/**
	 * UI에서 호스트를 구분하기 위한 목적으로 hostname, agentid, endpoint등 구분할 수 있는 아무거나 넣으면 됨.
	 */
	private final String host;
	private final ResponseHistogram histogram;

	public Host(String host, ServiceType serviceType) {
		this.host = host;
		this.histogram = new ResponseHistogram(host, serviceType);
	}

	public String getHost() {
		return host;
	}

	public ResponseHistogram getHistogram() {
		return histogram;
	}

	@Override
	public String getId() {
		return host;
	}

	@Override
	public Host mergeWith(Host host) {
		this.histogram.mergeWith(host.getHistogram());
		return this;
	}

	public String getJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\"").append(host).append("\",");
		sb.append("\"histogram\":").append(histogram.getJson());
		sb.append("}");
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Host [host=" + host + ", histogram=" + histogram + "]";
	}
}
