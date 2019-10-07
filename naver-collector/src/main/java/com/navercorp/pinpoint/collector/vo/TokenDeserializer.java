/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.vo;

import com.navercorp.pinpoint.grpc.security.TokenType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author Taejin Koo
 */
public class TokenDeserializer extends JsonDeserializer<Token> {

    @Override
    public Token deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode jsonNode = mapper.readTree(jsonParser);

        if (!jsonNode.isObject()) {
            return null;
        }

        String tokenKey = getTextNode(jsonNode, "token");

        PaaSOrganizationInfo organizationInfo = deserializeOrganizationInfo(jsonNode);

        long expiryTime = getLongNode(jsonNode, "expiryTime");
        String remoteAddress = getTextNode(jsonNode, "remoteAddress");
        String tokenTypeName = getTextNode(jsonNode, "tokenType");
        TokenType tokenType = TokenType.getValue(tokenTypeName);

        return new Token(tokenKey, organizationInfo, expiryTime, remoteAddress, tokenType);
    }

    private PaaSOrganizationInfo deserializeOrganizationInfo(JsonNode jsonNode) {
        JsonNode organizationInfoNode = jsonNode.get("organizationInfo");
        if (organizationInfoNode.isObject()) {
            String organization = getTextNode(organizationInfoNode, "organization");
            String databaseName = getTextNode(organizationInfoNode, "databaseName");
            String hbaseNameSpace = getTextNode(organizationInfoNode, "hbaseNameSpace");

            return new PaaSOrganizationInfo(organization, databaseName, hbaseNameSpace);
        } else {
            return null;
        }
    }

    private String getTextNode(JsonNode jsonNode, String key) {
        JsonNode childNode = jsonNode.get(key);
        if (childNode == null || !childNode.isTextual()) {
            return null;
        }

        String value = childNode.asText();
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return value;
    }

    private long getLongNode(JsonNode jsonNode, String key) {
        JsonNode childNode = jsonNode.get(key);
        if (childNode == null || !childNode.isLong()) {
            return -1;
        }

        return childNode.asLong(-1);
    }

}
