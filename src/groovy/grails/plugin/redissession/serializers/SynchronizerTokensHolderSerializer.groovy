package grails.plugin.redissession.serializers

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.codehaus.groovy.grails.web.servlet.mvc.SynchronizerTokensHolder
import org.springframework.context.ApplicationContext

import java.lang.reflect.Type

class SynchronizerTokensHolderSerializer implements JsonSerializer<SynchronizerTokensHolder>, JsonDeserializer<SynchronizerTokensHolder> {

    def gson = new Gson()
    ApplicationContext applicationContext

    SynchronizerTokensHolderSerializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext
    }

    def gsonService = applicationContext.gsonService

    JsonElement serialize(SynchronizerTokensHolder synchronizerTokensHolder, Type type, JsonSerializationContext context) {

        String objectString = gson.toJson(synchronizerTokensHolder)

        JsonObject result = new JsonObject()
        result.addProperty("type", "org.codehaus.groovy.grails.web.servlet.mvc.SynchronizerTokensHolder")

        result.addProperty("value", objectString)

        return result
    }

    SynchronizerTokensHolder deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        JsonParser jsonParser = gsonService.jsonParser

        JsonObject valueObject = jsonParser.parse(json.getAsJsonObject().get("value").value).getAsJsonObject()

        return gson.fromJson(valueObject, SynchronizerTokensHolder.class)
    }
}