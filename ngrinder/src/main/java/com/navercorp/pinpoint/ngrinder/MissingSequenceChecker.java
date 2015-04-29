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
package com.navercorp.pinpoint.ngrinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Jongho Moon
 *
 */
public class MissingSequenceChecker {
    private final List<Range> list = new ArrayList<Range>();
    
    public void add(long value) {
        Range newRange = new Range(value, value);
        
        int pos = Collections.binarySearch(list, newRange);
        
        if (pos >= 0) {
            return;
        }
        
        int nextPos = -(pos + 1);
        int prevPos = nextPos - 1;
        
        Range prev = prevPos >= 0 ? list.get(prevPos) : null;
        boolean prevContinuous = prev == null ? false : prev.to + 1 == value;
        
        Range next = nextPos < list.size() ? list.get(nextPos) : null;
        boolean nextContinuous = next == null ? false : value + 1 == next.from;
        
        if (prevContinuous) {
            if (nextContinuous) {
                prev.to = next.to;
                list.remove(nextPos);
            } else {
                prev.to = value;
            }
        } else {
            if (nextContinuous) {
                next.from = value;
            } else {
                list.add(nextPos, newRange);
            }
        }
    }
    
    public long getLast() {
        return list.get(list.size() - 1).to;
    }
    
    public long getMissingCount() {
        long missing = 0;
        long last = -1;
        
        for (Range r : list) {
            missing += r.from - last - 1;
            last = r.to;
        }
        
        return missing;
    }
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        long last = -1;
        
        for (Range r : list) {
            long missing = r.from - last - 1;
            
            if (missing != 0) {
                b.append("! ");
                b.append(missing);
                b.append(" !");
            }
            
            b.append('(');
            b.append(r.from);
            b.append("~");
            b.append(r.to);
            b.append(')');
            
            last = r.to;
        }
        
        return b.toString();
    }


    /**
     * @author Jongho Moon
     *
     */
    private static class Range implements Comparable<Range> {
        private long from;
        private long to;
        
        public Range(long from, long to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public int compareTo(Range o) {
            if (this.from > o.to) {
                return 1;
            } else if (this.to < o.from) {
                return -1;
            }
            
            return 0;
//            throw new IllegalArgumentException("this: " + this + ", that: " + o);
        }

        @Override
        public String toString() {
            return "Range [from=" + from + ", to=" + to + "]";
        }
    }
}
