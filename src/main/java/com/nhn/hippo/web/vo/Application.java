package com.nhn.hippo.web.vo;

import com.profiler.common.ServiceType;

/**
 * 
 * @author netspider
 * 
 */
public class Application {
	private final String applicationName;
	private final ServiceType serviceType;

	public Application(String applicationName, short serviceType) {
		this.applicationName = applicationName;
		this.serviceType = ServiceType.findServiceType(serviceType);
	}

	public String getApplicationName() {
		return applicationName;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	@Override
	public String toString() {
		return applicationName + "(" + serviceType + ")";
	}
}
