package org.skills.data.database.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Duration;

public class AdapterDuration implements JsonSerializer<Duration>, JsonDeserializer<Duration> {
    @Override
    public JsonElement serialize(Duration obj, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(obj.toMillis());
    }

    @Override
    public Duration deserialize(JsonElement obj, Type type, JsonDeserializationContext context) {
        return Duration.ofMillis(obj.getAsLong());
    }
}
