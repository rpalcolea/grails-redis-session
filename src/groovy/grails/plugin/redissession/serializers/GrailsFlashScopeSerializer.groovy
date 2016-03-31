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

        flashScope = flashScope as RedisGrailsFlashScope

        LinkedHashMap current = flashScope.getCurrent() as LinkedHashMap ?: [:]
        LinkedHashMap next = flashScope.getNext() as LinkedHashMap ?: [:]

        String currentMap = gsonService.serializeAsJson(current)
        String nextMap = gsonService.serializeAsJson(next)

        result.addProperty("currentMap", currentMap)
        result.addProperty("nextMap", nextMap)

        return result
    }

    GrailsFlashScope deserialize(JsonElement json, Type type, JsonDeserializationContext context) {

        RedisGrailsFlashScope flashScope = new RedisGrailsFlashScope()

        JsonParser jsonParser = gsonService.getJsonParser()

        JsonObject currentValueObject = jsonParser.parse(json.getAsJsonObject().get("currentMap").value).getAsJsonObject()
        JsonObject nextValueObject = jsonParser.parse(json.getAsJsonObject().get("nextMap").value).getAsJsonObject()

        LinkedHashMap current = gsonService.deserializeJson(currentValueObject)
        LinkedHashMap next = gsonService.deserializeJson(nextValueObject)

        flashScope.setCurrent(current)
        flashScope.setNext(next)

        return flashScope
    }
}