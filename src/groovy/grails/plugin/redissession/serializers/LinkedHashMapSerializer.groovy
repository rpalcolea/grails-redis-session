package grails.plugin.redissession.serializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import groovy.json.StringEscapeUtils
import org.springframework.context.ApplicationContext

import java.lang.reflect.Type


class LinkedHashMapSerializer implements JsonSerializer<LinkedHashMap>, JsonDeserializer<LinkedHashMap> {

    ApplicationContext applicationContext

    LinkedHashMapSerializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext
    }

    def gsonService = applicationContext.gsonService

    JsonElement serialize(LinkedHashMap linkedHashMap, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject()
        result.addProperty("type", "java.util.LinkedHashMap")

        JsonObject valObject = new JsonObject()
        linkedHashMap.each {
            valObject.addProperty(gsonService.serializeAsJson(it.key), gsonService.serializeAsJson(it.value))
        }

        result.addProperty("value", valObject.toString())
        return result
    }

    LinkedHashMap deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        JsonObject serializedObject = json.getAsJsonObject()

        LinkedHashMap returnMap = [:]

        JsonParser jsonParser = gsonService.getJsonParser()

        JsonObject jsonObject = jsonParser.parse(json.getAsJsonObject().get("value").value).getAsJsonObject()

        jsonObject.entrySet().each {
            String keyString = it.key
            //deal with troublesome string formatting when converting from JsonPrimitive
            String valueString = StringEscapeUtils.unescapeJava(it.value.toString())[1..-2]

            JsonObject keyObject = jsonParser.parse(keyString).getAsJsonObject()
            JsonObject valObject = jsonParser.parse(valueString).getAsJsonObject()

            returnMap.put(gsonService.deserializeJson(keyObject), gsonService.deserializeJson(valObject))
        }

        return returnMap
    }
}