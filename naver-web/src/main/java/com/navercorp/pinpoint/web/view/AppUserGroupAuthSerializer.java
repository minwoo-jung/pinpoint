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
package com.navercorp.pinpoint.web.view;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;

/**
 * @author minwoo.jung
 */
public class AppUserGroupAuthSerializer extends JsonSerializer<AppUserGroupAuth> {

    @Override
    public void serialize(AppUserGroupAuth appAuth, JsonGenerator jgen, SerializerProvider serializers)
        throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField("applicationId", appAuth.getApplicationId());
        jgen.writeStringField("userGroupId", appAuth.getUserGroupId());
        jgen.writeStringField("role", appAuth.getRole().toString());
        jgen.writeObjectField("configuration", appAuth.getConfiguration());
        jgen.writeEndObject();
    }

}
