package com.navercorp.pinpoint.plugin.lucy.net.npc;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;


/**
 * @author Taejin Koo
 */
public enum NpcHessianConnectorConditionType {

    V13to19("V13to19") {
        @Override
        public boolean isSupport(InstrumentClass target) {
            if (checkUpperVersionSupport(this, target)) {
                return false;
            }
            return target.hasDeclaredMethod("createConnecor", new String[] { "com.nhncorp.lucy.npc.connector.NpcConnectorOption" });
        }
    },
    V12("V12", V13to19) {
        @Override
        public boolean isSupport(InstrumentClass target) {
            if (checkUpperVersionSupport(this, target)) {
                return false;
            }
            return target.hasDeclaredMethod("initialize", new String[] { "com.nhncorp.lucy.npc.connector.NpcConnectorOption" });
        }
    },
    V11("V11", V12, V13to19) {
        @Override
        public boolean isSupport(InstrumentClass target) {
            if (checkUpperVersionSupport(this, target)) {
                return false;
            }
            return target.hasConstructor(new String[] { "com.nhncorp.lucy.npc.connector.NpcConnectorOption" });
        }
    },
    V6to10("V6to10", V11, V12, V13to19) {
        @Override
        public boolean isSupport(InstrumentClass target) {
            if (checkUpperVersionSupport(this, target)) {
                return false;
            }

            if (!target.hasConstructor(new String[] { "java.net.InetSocketAddress", "boolean", "boolean", "boolean", "java.nio.charset.Charset", "long" })) {
                return false;
            }
            if (!target.hasConstructor(new String[] { "java.net.InetSocketAddress", "com.nhncorp.lucy.npc.connector.ConnectionFactory"} )) {
                return false;
            }
            
            return true;
        }
    },
    V5("V5", V6to10, V11, V12, V13to19) {
        @Override
        public boolean isSupport(InstrumentClass target) {
            if (checkUpperVersionSupport(this, target)) {
                return false;
            }
            
            return target.hasConstructor(new String[] { "java.net.InetSocketAddress", "boolean", "boolean", "boolean", "java.nio.charset.Charset", "long" });
        }
    },
    UNKNOWN("UNKNOWN") {
        @Override
        public boolean isSupport(InstrumentClass target) {
            return false;
        }
    };
    
    public abstract boolean isSupport(InstrumentClass target);
    
    private final String versionName;
    private final List<NpcHessianConnectorConditionType> upperVersionConditionList = new ArrayList<NpcHessianConnectorConditionType>();
    
    private NpcHessianConnectorConditionType(String versionName, NpcHessianConnectorConditionType... upperVersionConditionList) {
        this.versionName = versionName;
        
        if (upperVersionConditionList != null) {
            for (NpcHessianConnectorConditionType upperVersionCondition : upperVersionConditionList) {
                this.upperVersionConditionList.add(upperVersionCondition);
            }
        }
    }

    private static boolean checkUpperVersionSupport(NpcHessianConnectorConditionType npcHessianConnectorCondition, InstrumentClass target) {
        for (NpcHessianConnectorConditionType upperVersionCondition : npcHessianConnectorCondition.upperVersionConditionList) {
            boolean isSupport = upperVersionCondition.isSupport(target);
            if (isSupport) {
                return true;
            }
        }
        
        return false;
    }
    
    static NpcHessianConnectorConditionType getCondition(String version) {
        if (version == null) {
            throw new NullPointerException("version may not be null.");
        }

        for (NpcHessianConnectorConditionType condition : NpcHessianConnectorConditionType.values()) {
            if (condition.versionName.equals(version)) {
                return condition;
            }
        }

        return UNKNOWN;

    }

}
