import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

import java.lang.reflect.Type

class HashSetSerializer implements JsonSerializer<HashSet>, JsonDeserializer<HashSet> {
    def gsonService = ServletContextHolder.servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT).gsonService

    JsonElement serialize(HashSet hashSet, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject()
        result.addProperty("type", "java.util.HashSet")

        JsonArray arrayJson = new JsonArray()
        hashSet.each {
            String itemJson = gsonService.serializeAsJson(it)
            JsonPrimitive jsonPrimitive = new JsonPrimitive(itemJson)
            arrayJson.add(jsonPrimitive)
        }

        String arrayString = arrayJson.toString()

        result.addProperty("value", arrayString)
        return result
    }

    HashSet deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        JsonObject serializedObject = json.getAsJsonObject()
        String jsonArrayString = serializedObject.get("value").value
        JsonParser parser = new JsonParser()
        JsonArray jsonArray = parser.parse(jsonArrayString)

        HashSet returnHashSet = new HashSet()

        for (int i = 0; i < jsonArray.size(); i++) {
            String jsonString = jsonArray.get(i).value
            JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject()
            def parsedObject = gsonService.deserializeJson(jsonObject)
            returnHashSet.add(parsedObject)
        }
        return returnHashSet
    }
}