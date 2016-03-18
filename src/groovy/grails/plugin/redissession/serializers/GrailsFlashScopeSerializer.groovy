package grails.plugin.redissession.serializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import grails.plugin.redissession.RedisGrailsFlashScope
import org.codehaus.groovy.grails.web.servlet.GrailsFlashScope
import org.springframework.context.ApplicationContext

import java.lang.reflect.Type

class GrailsFlashScopeSerializer implements JsonSerializer<GrailsFlashScope>, JsonDeserializer<GrailsFlashScope> {

    ApplicationContext applicationContext

    GrailsFlashScopeSerializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext
    }

    def gsonService = applicationContext.gsonService

    JsonElement serialize(GrailsFlashScope flashScope, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject()
        result.addProperty("type", "org.codehaus.groovy.grails.web.servlet.GrailsFlashScope")

        LinkedHashMap values = [:]

        flashScope.keySet().each {
            values.put(it, flashScope.get(it))
        }

        String mapJson = gsonService.serializeAsJson(values)
        result.addProperty("value", mapJson)

        return result
    }

    GrailsFlashScope deserialize(JsonElement json, Type type, JsonDeserializationContext context) {

        RedisGrailsFlashScope flashScope = new RedisGrailsFlashScope()

        JsonParser jsonParser = gsonService.getJsonParser()

        JsonObject valueObject = jsonParser.parse(json.getAsJsonObject().get("value").value).getAsJsonObject()
        LinkedHashMap deserialized = gsonService.deserializeJson(valueObject)

        deserialized.each { key, value ->
            flashScope.put(key, value)
        }

        return flashScope
    }
}