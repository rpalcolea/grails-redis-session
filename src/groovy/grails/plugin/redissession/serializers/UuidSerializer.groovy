package grails.plugin.redissession.serializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer

import java.lang.reflect.Type

class UuidSerializer implements JsonSerializer<UUID>, JsonDeserializer<UUID> {

    JsonElement serialize(UUID uuid, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject()
        result.addProperty("type", "java.util.UUID")
        result.addProperty("value", uuid.toString())
        return result
    }

    UUID deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject()
        return UUID.fromString(jsonObject.get("value").getAsString())
    }
}