/*
 * Copyright 2017 NAVER Corp.
 *
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

package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import com.navercorp.pinpoint.web.calltree.span.Align;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author HyunGil Jeong
 */
public class CallStackVerifier {

    private final SpanElement root;

    public CallStackVerifier(TSpan rootSpan) {
        if (rootSpan == null) {
            throw new NullPointerException("rootSpan");
        }
        root = new SpanElement(0, rootSpan.getSpanId());
    }

    public void addChildSpan(TSpan parentSpan, TSpan childSpan) {
        if (!root.addChildSpan(parentSpan, childSpan)) {
            throw new IllegalStateException("No matching parent span found");
        }
    }

    public List<CallStackElement> values() {
        return root.flatten();
    }

    public abstract class CallStackElement {
        final int depth;
        final boolean isSpan;

        CallStackElement(int depth, boolean isSpan) {
            this.depth = depth;
            this.isSpan = isSpan;
        }

        public abstract void verifySpanAlign(Align align);

        protected abstract List<CallStackElement> flatten();
    }

    private class SpanElement extends CallStackElement {
        private final SpanElement parentSpan;
        private final long spanId;
        private final Map<Integer, SpanEventElement> orderedSpanEvents = new TreeMap<>();

        private SpanElement(int depth, long spanId) {
            this(null, depth, spanId);
        }

        private SpanElement(SpanElement parentSpan, int depth, long spanId) {
            super(depth, true);
            this.parentSpan = parentSpan;
            this.spanId = spanId;
        }

        long getSpanId() {
            return spanId;
        }

        boolean addChildSpan(TSpan parentSpan, TSpan childSpan) {
            if (parentSpan.getSpanId() == this.spanId) {
                long childSpanId = childSpan.getSpanId();
                for (TSpanEvent clientSpanEvent : parentSpan.getSpanEventList()) {
                    if (clientSpanEvent.getNextSpanId() == childSpanId) {
                        int sequence = clientSpanEvent.getSequence();
                        int spanEventDepth = this.depth + 1;
                        int childSpanDepth = spanEventDepth + 1;
                        SpanElement childSpanElement = new SpanElement(this, childSpanDepth, childSpan.getSpanId());
                        SpanEventElement spanEventElement = new SpanEventElement(spanEventDepth, childSpanElement);
                        orderedSpanEvents.put(sequence, spanEventElement);
                        return true;
                    }
                }
            } else {
                for (SpanEventElement spanEvent : orderedSpanEvents.values()) {
                    if (spanEvent.hasNextSpan()) {
                        SpanElement nextSpan = spanEvent.getNextSpan();
                        if (nextSpan.addChildSpan(parentSpan, childSpan)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void verifySpanAlign(Align align) {
            Assert.assertEquals(isSpan, align.isSpan());
            Assert.assertEquals(depth, align.getDepth());
            Assert.assertEquals(spanId, align.getSpanId());
            long parentSpanId = align.getSpanBo().getParentSpanId();
            if (parentSpanId == -1) {
                Assert.assertNull(parentSpan);
            } else {
                Assert.assertNotNull(parentSpan);
                Assert.assertEquals(parentSpan.getSpanId(), parentSpanId);
            }
        }

        @Override
        protected List<CallStackElement> flatten() {
            List<CallStackElement> elements = new ArrayList<>();
            elements.add(this);
            for (SpanEventElement spanEvent : orderedSpanEvents.values()) {
                elements.addAll(spanEvent.flatten());
            }
            return elements;
        }
    }

    private class SpanEventElement extends CallStackElement {

        private final SpanElement nextSpan;

        private SpanEventElement(int depth, SpanElement nextSpan) {
            super(depth, false);
            this.nextSpan = nextSpan;
        }

        boolean hasNextSpan() {
            return nextSpan != null;
        }

        SpanElement getNextSpan() {
            return nextSpan;
        }

        @Override
        public void verifySpanAlign(Align align) {
            Assert.assertEquals(isSpan, align.isSpan());
            Assert.assertEquals(depth, align.getDepth());
            long nextSpanId = align.getSpanEventBo().getNextSpanId();
            if (nextSpanId == -1) {
                Assert.assertNull(nextSpan);
            } else {
                Assert.assertNotNull(nextSpan);
                Assert.assertEquals(nextSpan.getSpanId(), nextSpanId);
            }
        }

        @Override
        protected List<CallStackElement> flatten() {
            List<CallStackElement> elements = new ArrayList<>();
            elements.add(this);
            if (nextSpan != null) {
                elements.addAll(nextSpan.flatten());
            }
            return elements;
        }
    }
}
