package grails.plugin.redissession.serializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonDeserializer

import java.lang.reflect.Type

class BooleanSerializer implements JsonSerializer<Boolean>, JsonDeserializer<Boolean> {

    JsonElement serialize(Boolean val, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject()
        result.addProperty("type", "java.lang.Boolean")
        result.addProperty("value", val)
        return result
    }

    Boolean deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        JsonObject jobject = json.getAsJsonObject()
        return jobject.get("value").getAsBoolean()
    }
}
