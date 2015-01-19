package com.navercorp.pinpoint.profiler.util;

import java.util.HashMap;
import java.util.Map;

import com.navercorp.pinpoint.bootstrap.instrument.Scope;

public class DepthScopeKeepingData implements Scope {

    public static final int ZERO = 0;

    private final NamedThreadLocal<Depth> scope;
    
    private final NamedThreadLocal<Map<Object, Object>> data;

    public DepthScopeKeepingData(final String scopeName) {
        this.scope = new NamedThreadLocal<Depth>(scopeName) {
            @Override
            protected Depth initialValue() {
                return new Depth();
            }
        };
        
        this.data = new NamedThreadLocal<Map<Object, Object>>(scopeName) {
            @Override
            protected Map<Object, Object> initialValue() {
                return new HashMap<Object, Object>();
            }
        };
    }

    @Override
    public int push() {
        final Depth depth = scope.get();
        return depth.push();
    }

    @Override
    public int depth() {
        final Depth depth = scope.get();
        return depth.depth();
    }

    @Override
    public int pop() {
        final Depth depth = scope.get();
        return depth.pop();
    }
    
    public Map<Object, Object> getData() {
        return data.get();
    }
    
    public void clearData() {
        data.get().clear();
    }

    private static final class Depth {
        private int depth = 0;

        public int push() {
            return depth++;
        }

        public int pop() {
            return --depth;
        }

        public int depth() {
            return depth;
        }

    }

    @Override
    public String getName() {
        return scope.getName();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DepthScope{");
        sb.append("scope=").append(scope.getName());
        sb.append('}');
        return sb.toString();
    }
}
