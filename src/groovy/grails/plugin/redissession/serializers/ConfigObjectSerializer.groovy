package grails.plugin.redissession.serializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.springframework.context.ApplicationContext

import java.lang.reflect.Type

class ConfigObjectSerializer implements JsonSerializer<ConfigObject>, JsonDeserializer<ConfigObject> {

    ApplicationContext applicationContext

    ConfigObjectSerializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext
    }

    def gsonService = applicationContext.gsonService

    JsonElement serialize(ConfigObject configObject, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject()
        result.addProperty("type", "groovy.util.ConfigObject")
        LinkedHashMap values = configObject as LinkedHashMap
        String mapJson = gsonService.serializeAsJson(values)
        result.addProperty("value", mapJson)
        return result
    }

    ConfigObject deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        ConfigObject configObject = new ConfigObject()
        JsonParser jsonParser = gsonService.getJsonParser()

        JsonObject valueObject = jsonParser.parse(json.getAsJsonObject().get("value").value).getAsJsonObject()
        LinkedHashMap deserialized = gsonService.deserializeJson(valueObject)

        deserialized.each { key, value ->
            configObject.put(key, value)
        }

        return configObject
    }
}