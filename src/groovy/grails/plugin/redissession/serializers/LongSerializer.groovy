package grails.plugin.redissession.serializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonDeserializer

import java.lang.reflect.Type

class LongSerializer implements JsonSerializer<Long>, JsonDeserializer<Long> {

    JsonElement serialize(Long longNum, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject()
        result.addProperty("type", "java.lang.Long")
        result.addProperty("value", longNum)
        return result
    }

    Long deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        JsonObject jobject = json.getAsJsonObject()
        return jobject.get("value").getAsLong()
    }
}