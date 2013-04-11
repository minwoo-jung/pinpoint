package com.profiler.common.dto;

import java.io.Serializable;

public class AgentInfoDTO implements Serializable {

	private static final long serialVersionUID = 1465266151876398515L;

	public AgentInfoDTO(String ip, String portNumbers) {
		hostHashCode = (hostIP + portNumbers).hashCode();
		timestamp = System.currentTimeMillis();
	}

	private String hostIP, portNumbers;
	private int hostHashCode;
	private boolean isAlive = true;
	private long timestamp;

	public void setIsDead() {
		isAlive = false;
	}

	public String toString() {
		return hostHashCode + " " + hostIP + " " + portNumbers + " " + portNumbers + " isAlive=" + isAlive;
	}

	public String getHostIP() {
		return hostIP;
	}

	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}

	public int getHostHashCode() {
		return hostHashCode;
	}

	public void setHostHashCode(int hostHashCode) {
		this.hostHashCode = hostHashCode;
	}

	public String getPortNumbers() {
		return portNumbers;
	}

	public void setPortNumbers(String portNumber) {
		this.portNumbers = portNumber;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean isAlive() {
		return isAlive;
	}
}
