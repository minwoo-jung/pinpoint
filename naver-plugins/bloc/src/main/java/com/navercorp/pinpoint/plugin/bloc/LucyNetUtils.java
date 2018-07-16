/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.nhncorp.lucy.net.call.Call;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress;
import com.nhncorp.lucy.npc.NpcMessage;
import com.nhncorp.lucy.npc.UserOptionIndex;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class LucyNetUtils {

    private static final int MAX_OPTION_SIZE_EACH_SET = 32;

    private static final String NAMESPACE_URA = "URA 1.0";

    public static Map<String, String> getPinpointOptions(NpcMessage npcMessage) {
        return getPinpointOptions(npcMessage, npcMessage.getUserOptionCount(), npcMessage.getUserOptionFlagCount());
    }

    public static Map<String, String> getPinpointOptions(NpcMessage npcMessage, int optionSetCount, int optionsCount) {
        if (optionsCount == 0) {
            return Collections.emptyMap();
        }

        Map<String, String> pinpointOptions = new HashMap<String, String>(optionsCount);
        for (int i = 1; i <= optionSetCount; i++) {
            for (int j = 0; j < MAX_OPTION_SIZE_EACH_SET; j++) {
                UserOptionIndex optionIndex = new UserOptionIndex(i, j);

                try {
                    byte[] userOption = npcMessage.getUserOption(optionIndex);
                    if (userOption != null) {
                        String[] keyValuePair = new String(userOption, BlocConstants.UTF_8_CHARSET).split("=");
                        if (keyValuePair.length == 2) {
                            String key = keyValuePair[0];
                            String value = keyValuePair[1];

                            boolean isPinpointHeader = LucyNetHeader.hasHeader(key);
                            if (isPinpointHeader) {
                                pinpointOptions.put(key, value);
                            }
                        }
                    }

                    if (pinpointOptions.size() == optionsCount) {
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return pinpointOptions;
    }

    public static String getRpcName(NpcMessage npcMessage) {
        if (npcMessage == null) {
            return "//";
        }

        Object call = npcMessage.getPayload();
        if (call instanceof Call) {
            StringBuilder rpcNameBuilder = new StringBuilder("/");

            String namespace = npcMessage.getNamespace();
            String objectName = ((Call) call).getObjectName();
            String methodName = ((Call) call).getMethodName();
            if (namespace.equals(NAMESPACE_URA)) {
                rpcNameBuilder.append(methodName);
            } else {
                rpcNameBuilder.append(objectName);
                rpcNameBuilder.append('/');
                rpcNameBuilder.append(methodName);
            }
            return rpcNameBuilder.toString();
        } else {
            return "";
        }
    }

    public static String nimmAddressToString(NimmAddress nimmAddress) {
        if (nimmAddress == null) {
            return BlocConstants.UNKNOWN_ADDRESS;
        }

        return nimmAddress.getDomainId() + "." + nimmAddress.getIdcId() + "." + nimmAddress.getServerId() + "."+ nimmAddress.getSocketId();
    }

    public static String getParameterAsString(List<Object> params, int eachLimit, int maxLimit) {
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
        if (params.size() == 1 && params.get(0) instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) params.get(0);

            for (Map.Entry<?, ?> entry : map.entrySet()) {
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
                final String key = String.valueOf(entry.getKey());
                paramsAsString.append(StringUtils.abbreviate(key, Math.min(eachLimit, remainSize)));
                paramsAsString.append('=');

                remainSize = Math.max(maxLimit - paramsAsString.length(), 1);
                final String value = String.valueOf(entry.getValue());
                paramsAsString.append(StringUtils.abbreviate(value, Math.min(eachLimit, remainSize)));
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
