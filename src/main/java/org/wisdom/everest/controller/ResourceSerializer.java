package org.wisdom.everest.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.ow2.chameleon.everest.services.Path;
import org.ow2.chameleon.everest.services.Relation;
import org.ow2.chameleon.everest.services.Resource;

import java.io.IOException;
import java.util.Map;

/**
 * Created by clement on 10/02/2014.
 */
public class ResourceSerializer extends JsonSerializer<Resource> {

    @Override
    public Class<Resource> handledType() {
        return Resource.class;
    }

    @Override
    public void serialize(Resource resource, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws
            IOException, JsonProcessingException {
        System.out.println("Serializing " + resource.getPath() + " ( " + resource.getMetadata() + " )");
        jsonGenerator.writeStartObject();
        if (resource.getPath() != null) {
            jsonGenerator.writeStringField("path", resource.getPath().toString());
        }
        if (resource.getCanonicalPath() != null) {
            jsonGenerator.writeStringField("canonicalPath", resource.getCanonicalPath().toString());
        }
        jsonGenerator.writeBooleanField("observable", resource.isObservable());

        jsonGenerator.writeFieldName("metadata");
        System.out.println(resource.getMetadata());
        serializerProvider.defaultSerializeValue(resource.getMetadata(), jsonGenerator);

        jsonGenerator.writeFieldName("relations");
        jsonGenerator.writeStartObject();
        for (Object relation : resource.getRelations()) {
            jsonGenerator.writeFieldName(((Relation) relation).getName());
            serializerProvider.defaultSerializeValue(relation, jsonGenerator);
        }
        jsonGenerator.writeEndObject();

        jsonGenerator.writeEndObject();
    }
}
