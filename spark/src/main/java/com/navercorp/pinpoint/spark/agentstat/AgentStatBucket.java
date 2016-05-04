/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.spark.agentstat;

import java.io.Serializable;

/**
 * @author Jongho Moon
 *
 */
public class AgentStatBucket implements Serializable {
    private static final long serialVersionUID = -4538837612829485452L;

    private final String agentId;
    private final long timestamp;
    
    public AgentStatBucket(String agentId, long timestamp) {
        this.agentId = agentId;
        this.timestamp = timestamp;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + agentId.hashCode();
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AgentStatBucket)) {
            return false;
        }
        AgentStatBucket other = (AgentStatBucket) obj;
        
        if (!agentId.equals(other.agentId)) {
            return false;
        }
        if (timestamp != other.timestamp) {
            return false;
        }
        
        return true;
    }

    
    
}
