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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.common.util.Assert;

import java.io.IOException;

/**
 * @author Taejin Koo
 */
public class TokenSerializer extends JsonSerializer<Token> {

    @Override
    public void serialize(Token token, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();

        jgen.writeStringField("token", token.getKey());

        PaaSOrganizationInfo paaSOrganizationInfo = token.getPaaSOrganizationInfo();
        serializeOrganizationInfo(paaSOrganizationInfo, jgen);

        jgen.writeNumberField("expiryTime", token.getExpiryTime());
        jgen.writeStringField("remoteAddress", token.getRemoteAddress());
        jgen.writeStringField("tokenType", token.getTokenType().name());

        jgen.writeEndObject();
    }

    private void serializeOrganizationInfo(PaaSOrganizationInfo paaSOrganizationInfo, JsonGenerator jgen) throws IOException {
        Assert.requireNonNull(paaSOrganizationInfo, "paaSOrganizationInfo");

        jgen.writeObjectFieldStart("organizationInfo");

        jgen.writeStringField("organization", paaSOrganizationInfo.getOrganization());
        jgen.writeStringField("databaseName", paaSOrganizationInfo.getDatabaseName());
        jgen.writeStringField("hbaseNameSpace", paaSOrganizationInfo.getHbaseNameSpace());

        jgen.writeEndObject();
    }

}
