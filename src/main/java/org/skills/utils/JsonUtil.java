package org.skills.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;

public final class JsonUtil {
    public static JsonElement from(Reader reader) {
        try {
            JsonReader jsonReader = new JsonReader(reader);
            JsonElement element = parseReader(jsonReader);
            if (!element.isJsonNull() && jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonSyntaxException("Did not consume the entire document.");
            } else {
                return element;
            }
        } catch (MalformedJsonException | NumberFormatException ex) {
            throw new JsonSyntaxException(ex);
        } catch (IOException ex) {
            throw new JsonIOException(ex);
        }
    }

    public static JsonElement fromString(String json) {
        // JsonParser.parseString(json) doesn't exist for older versions of GSON.
        return from(new StringReader(json));
    }

    private static JsonElement parseReader(JsonReader reader) throws JsonIOException, JsonSyntaxException {
        try {
            return Streams.parse(reader);
        } catch (StackOverflowError | OutOfMemoryError ex) {
            throw new JsonParseException("Failed parsing JSON source: " + reader + " to Json", ex);
        }
    }

    public static String toString(JsonElement element) {
        Objects.requireNonNull(element);
        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.setLenient(false);
            jsonWriter.setSerializeNulls(false);
            jsonWriter.setHtmlSafe(false);
            Streams.write(element, jsonWriter);
            return stringWriter.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
