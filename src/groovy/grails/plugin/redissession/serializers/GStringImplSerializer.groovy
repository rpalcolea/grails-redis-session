package grails.plugin.redissession.serializers

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.codehaus.groovy.runtime.GStringImpl

import java.lang.reflect.Type

class GStringImplSerializer implements JsonSerializer<GStringImpl> {

    JsonElement serialize(GStringImpl gString, Type type, JsonSerializationContext context) {
        //Just serialize as String
        JsonObject result = new JsonObject()
        result.addProperty("type", "java.lang.String")
        result.addProperty("value", gString.toString())
        return result
    }
}