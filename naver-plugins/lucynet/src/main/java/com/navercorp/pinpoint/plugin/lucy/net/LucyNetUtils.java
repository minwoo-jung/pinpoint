/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.lucy.net;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;
import com.nhncorp.lucy.npc.UserOptionIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public final class LucyNetUtils {

    private static final String PINPOINT_TRACE_ID = LucyNetHeader.PINPOINT_TRACE_ID.toString();
    private static final String PINPOINT_SPAN_ID = LucyNetHeader.PINPOINT_SPAN_ID.toString();
    private static final String PINPOINT_PARENT_SPAN_ID = LucyNetHeader.PINPOINT_PARENT_SPAN_ID.toString();
    private static final String PINPOINT_FLAGS = LucyNetHeader.PINPOINT_FLAGS.toString();
    private static final String PINPOINT_PARENT_APPLICATION_NAME = LucyNetHeader.PINPOINT_PARENT_APPLICATION_NAME.toString();
    private static final String PINPOINT_PARENT_APPLICATION_TYPE = LucyNetHeader.PINPOINT_PARENT_APPLICATION_TYPE.toString();
    private static final String PINPOINT_HOST = LucyNetHeader.PINPOINT_HOST.toString();
    private static final String PINPOINT_SAMPLED = LucyNetHeader.PINPOINT_SAMPLED.toString();

    private LucyNetUtils() {
    }

    public static List<byte[]> createOptions(TraceId nextTraceId, String applicationName, short serverTypeCode, String endPoint) {
        return createOptions(nextTraceId, applicationName, serverTypeCode, endPoint, Collections.<String>emptySet());
    }

    public static List<byte[]> createOptions(TraceId nextTraceId, String applicationName, short serverTypeCode, String endPoint, Set<String> notIncludeOptions) {
        if (notIncludeOptions == null) {
            throw new NullPointerException("notIncludeOptions must not be null.");
        }

        if (nextTraceId == null) {
            return Collections.emptyList();
        }


        StringBuilder optionStringBuilder = new StringBuilder(32);

        List<byte[]> options = new ArrayList<byte[]>(8);

        if (!notIncludeOptions.contains(PINPOINT_TRACE_ID)) {
            options.add(stringToBytes(PINPOINT_TRACE_ID, nextTraceId.getTransactionId(), optionStringBuilder));
        }

        if (!notIncludeOptions.contains(PINPOINT_SPAN_ID)) {
            options.add(stringToBytes(PINPOINT_SPAN_ID, Long.toString(nextTraceId.getSpanId()), optionStringBuilder));
        }

        if (!notIncludeOptions.contains(PINPOINT_PARENT_SPAN_ID)) {
            options.add(stringToBytes(PINPOINT_PARENT_SPAN_ID, Long.toString(nextTraceId.getParentSpanId()), optionStringBuilder));
        }

        if (!notIncludeOptions.contains(PINPOINT_FLAGS)) {
            options.add(stringToBytes(PINPOINT_FLAGS, Short.toString(nextTraceId.getFlags()), optionStringBuilder));
        }

        if (!notIncludeOptions.contains(PINPOINT_PARENT_APPLICATION_NAME)) {
            options.add(stringToBytes(PINPOINT_PARENT_APPLICATION_NAME, applicationName, optionStringBuilder));
        }

        if (!notIncludeOptions.contains(PINPOINT_PARENT_APPLICATION_TYPE)) {
            options.add(stringToBytes(PINPOINT_PARENT_APPLICATION_TYPE, Short.toString(serverTypeCode), optionStringBuilder));
        }

        if (!notIncludeOptions.contains(PINPOINT_HOST)) {
            options.add(stringToBytes(PINPOINT_HOST, endPoint, optionStringBuilder));
        }

        if (!notIncludeOptions.contains(PINPOINT_SAMPLED)) {
            options.add(stringToBytes(PINPOINT_SAMPLED, SamplingFlagUtils.SAMPLING_RATE_TRUE, optionStringBuilder));
        }

        return options;
    }


    public static List<byte[]> createUnsampledOptions() {
        return createUnsampledOptions(Collections.<String>emptySet());
    }

    public static List<byte[]> createUnsampledOptions(Set<String> notIncludeOptions) {
        List<byte[]> options = new ArrayList<byte[]>(1);

        if (!notIncludeOptions.contains(PINPOINT_SAMPLED)) {
            String option = PINPOINT_SAMPLED + "=" + SamplingFlagUtils.SAMPLING_RATE_FALSE;
            options.add(option.getBytes(LucyNetConstants.UTF_8_CHARSET));
        }
        return options;
    }

    private static byte[] stringToBytes(String key, String value, StringBuilder stringBuilder) {
        stringBuilder.setLength(0);
        stringBuilder.append(key).append("=").append(value);
        return stringBuilder.toString().getBytes(LucyNetConstants.UTF_8_CHARSET);
    }

    public static UserOptionIndex increaseUserOptionIndex(UserOptionIndex userOptionIndex) {
        int optionSetIndex = userOptionIndex.getOptionSetIndex();
        int flagIndex = userOptionIndex.getFlagIndex() + 1;
        if (flagIndex == 32) {
            return new UserOptionIndex(optionSetIndex + 1, 0);
        } else {
            return new UserOptionIndex(optionSetIndex, flagIndex);
        }
    }

    public static boolean equalsUserOptionIndex(UserOptionIndex userOptionIndex1, UserOptionIndex userOptionIndex2) {
        if (userOptionIndex1 == null || userOptionIndex2 == null) {
            throw new NullPointerException("argument must not be null.");
        }

        if (userOptionIndex1.getFlagIndex() != userOptionIndex2.getFlagIndex()) {
            return false;
        }

        if (userOptionIndex1.getOptionSetIndex() != userOptionIndex2.getOptionSetIndex()) {
            return false;
        }

        return true;
    }

    public static String getParameterAsString(Object[] params, int eachLimit, int maxLimit) {
        if (eachLimit <= 0 || maxLimit <= 0) {
            throw new IllegalArgumentException("negative number exception.");
        }

        if (eachLimit > maxLimit) {
            throw new IllegalArgumentException("maxLimit must greater than eachLimit.");
        }

        if (params == null) {
            return "null";
        }

        final StringBuilder paramsAsString = new StringBuilder(64);
        if (params.length == 1 && params[0] instanceof Map) {
            Map map = (Map) params[0];

            Set keySet = map.keySet();
            for (Object key : keySet) {
                if (paramsAsString.length() == 0) {
                    paramsAsString.append("{ ");
                } else {
                    paramsAsString.append(", ");
                }

                if (paramsAsString.length() >= maxLimit) {
                    paramsAsString.append("...");
                    break;
                }

                int remainSize = Math.max(maxLimit - paramsAsString.length(), 1);
                paramsAsString.append(StringUtils.abbreviate(String.valueOf(key), Math.min(eachLimit, remainSize)));
                paramsAsString.append("=");

                remainSize = Math.max(maxLimit - paramsAsString.length(), 1);
                paramsAsString.append(StringUtils.abbreviate(String.valueOf(map.get(key)), Math.min(eachLimit, remainSize)));
            }
            paramsAsString.append(" }");
        } else {
            for (Object param : params) {
                if (paramsAsString.length() == 0) {
                    paramsAsString.append("[ ");
                } else {
                    paramsAsString.append(", ");
                }

                if (paramsAsString.length() >= maxLimit) {
                    paramsAsString.append("...");
                    break;
                }

                int remainSize = Math.max(maxLimit - paramsAsString.length(), 1);
                paramsAsString.append(StringUtils.abbreviate(String.valueOf(param), Math.min(eachLimit, remainSize)));
            }
            paramsAsString.append(" ]");
        }

        return paramsAsString.toString();
    }

}
