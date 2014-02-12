package com.nhn.pinpoint.web.applicationmap.rawdata;

import java.util.Set;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.vo.Application;

/**
 * DB에서 조회한 application호출 관계 정보.
 * 
 * @author netspider
 * @author emeroad
 */
public class LinkStatistics {

    private Application fromApplication;
    private Application toApplication;

	/**
	 * key = hostname
	 */
    private HostList toHostList;

    private Set<AgentInfoBo> toAgentSet;

	public LinkStatistics(Application fromApplication, Application toApplication) {
        if (fromApplication == null) {
            throw new NullPointerException("fromAppliation must not be null");
        }
        if (toApplication == null) {
            throw new NullPointerException("toApplication must not be null");
        }
        this.fromApplication = fromApplication;
		this.toApplication = toApplication;
        this.toHostList = new HostList();
	}


	/**
	 * 
	 * @param hostname
	 *            host이름 또는 endpoint
	 * @param slot
	 * @param value
	 */
	public void addSample(String hostname, short serviceTypeCode, short slot, long value) {
		// TODO 임시코드
		if (hostname == null || hostname.length() == 0) {
			hostname = "UNKNOWNHOST";
		}
        this.toHostList.addHost(hostname, serviceTypeCode, slot, value);
	}


    public Application getFromApplication() {
        return this.fromApplication;
    }

	public String getFrom() {
        return fromApplication.getName();
	}

    public Application getToApplication() {
        return this.toApplication;
    }
	public String getTo() {
		return toApplication.getName();
	}

	public ServiceType getFromServiceType() {
		return fromApplication.getServiceType();
	}

	public ServiceType getToServiceType() {
        return toApplication.getServiceType();
	}

    public void setFromApplication(Application fromApplication) {
        this.fromApplication = fromApplication;
    }

    public void setToApplication(Application toApplication) {
        this.toApplication = toApplication;
    }

	public HostList getToHostList() {
		return toHostList;
	}

	public Set<AgentInfoBo> getToAgentSet() {
		return toAgentSet;
	}

	public void addToAgentSet(Set<AgentInfoBo> agentSet) {
		if (this.toAgentSet != null) {
			this.toAgentSet.addAll(agentSet);
		} else {
			this.toAgentSet = agentSet;
		}
	}

	public void add(LinkStatistics applicationStatistics) {
        if (applicationStatistics == null) {
            throw new NullPointerException("applicationStatistics must not be null");
        }
        if (!this.equals(applicationStatistics)) {
            throw new IllegalArgumentException("Can't merge with different link.");
		}
        final HostList target = applicationStatistics.getToHostList();
        this.toHostList.addHostList(target);
	}

    @Override
    public String toString() {
        return "LoadFactor{" +
                "toHostList=" + toHostList +
                ", fromApplication=" + fromApplication +
                ", toApplication=" + toApplication +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkStatistics that = (LinkStatistics) o;

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
