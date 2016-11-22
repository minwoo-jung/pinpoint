package com.navercorp.pinpoint.web.view;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth;
import com.navercorp.pinpoint.web.vo.AppUserGroupAuth.Role;

public class AppUserGroupAuthSerializer extends JsonSerializer<AppUserGroupAuth> {

    @Override
    public void serialize(AppUserGroupAuth appAuth, JsonGenerator jgen, SerializerProvider serializers)
        throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField("applicationId", appAuth.getApplicationId());
        jgen.writeStringField("userGroupId", appAuth.getUserGroupId());
        jgen.writeObjectField("configuration", appAuth.getConfiguration());

        Role role = appAuth.getRole();
        if (role == null) {
            throw new JsonGenerationException("user Role is empty");
        }
        jgen.writeStringField("role", role.toString());

        jgen.writeEndObject();

    }

}
