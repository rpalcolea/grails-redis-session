package grails.plugin.redissession.serializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonDeserializer

import java.lang.reflect.Type

class IntegerSerializer implements JsonSerializer<Integer>, JsonDeserializer<Integer> {

    JsonElement serialize(Integer integer, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject()
        result.addProperty("type", "java.lang.Integer")
        result.addProperty("value", integer)
        return result
    }

    Integer deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        JsonObject jobject = json.getAsJsonObject()
        return jobject.get("value").getAsInt()
    }
}