package com.nhn.hippo.web.vo;

public class TerminalRequest {

	private final String from;
	private final String to;
	private final short toServiceType;
	private final int requestCount;

	public TerminalRequest(String from, String to, short toServiceType, int requestCount) {
		this.from = from;
		this.to = to;
		this.toServiceType = toServiceType;
		this.requestCount = requestCount;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public int getRequestCount() {
		return requestCount;
	}

	public short getToServiceType() {
		return toServiceType;
	}

	@Override
	public String toString() {
		return "{From=" + from + ", To=" + to + ", ReqCount=" + requestCount + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + requestCount;
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		result = prime * result + toServiceType;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TerminalRequest other = (TerminalRequest) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (requestCount != other.requestCount)
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		if (toServiceType != other.toServiceType)
			return false;
		return true;
	}
}
