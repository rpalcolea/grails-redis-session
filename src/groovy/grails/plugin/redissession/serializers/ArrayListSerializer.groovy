package grails.plugin.redissession.serializers

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonDeserializer
import org.springframework.context.ApplicationContext

import java.lang.reflect.Type

class ArrayListSerializer implements JsonSerializer<ArrayList>, JsonDeserializer<ArrayList> {

    ApplicationContext applicationContext

    ArrayListSerializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext
    }

    def gsonService = applicationContext.gsonService

    JsonElement serialize(ArrayList arrayList, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject()
        result.addProperty("type", "java.util.ArrayList")

        JsonArray arrayJson = new JsonArray()
        arrayList.each {
            String itemJson = gsonService.serializeAsJson(it)
            JsonPrimitive jsonPrimitive = new JsonPrimitive(itemJson)
            arrayJson.add(jsonPrimitive)
        }

        String arrayString = arrayJson.toString()

        result.addProperty("value", arrayString)
        return result
    }

    ArrayList deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        JsonObject serializedObject = json.getAsJsonObject()
        String jsonArrayString = serializedObject.get("value").value
        JsonParser jsonParser = new JsonParser()
        JsonArray jsonArray = jsonParser.parse(jsonArrayString)

        ArrayList returnList = []

        for (int i = 0; i < jsonArray.size(); i++) {
            String jsonString = jsonArray.get(i).value
            JsonObject jsonObject= jsonParser.parse(jsonString).getAsJsonObject()
            def parsedObject = gsonService.deserializeJson(jsonObject)
            returnList.add(parsedObject)
        }
        return returnList
    }
}