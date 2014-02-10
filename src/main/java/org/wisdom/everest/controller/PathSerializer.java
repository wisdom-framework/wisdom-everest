package org.wisdom.everest.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.ow2.chameleon.everest.services.Path;

import java.io.IOException;

/**
 * Created by clement on 10/02/2014.
 */
public class PathSerializer extends JsonSerializer<Path> {

    @Override
    public Class<Path> handledType() {
        return Path.class;
    }

    @Override
    public void serialize(Path path, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws
            IOException, JsonProcessingException {
        jsonGenerator.writeString("/everest" + path.toString());
    }
}
