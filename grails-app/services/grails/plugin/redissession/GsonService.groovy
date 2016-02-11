package grails.plugin.redissession

import com.google.gson.GsonBuilder
import com.google.gson.Gson
import com.google.gson.JsonObject
import grails.plugin.redissession.serializers.*
import org.codehaus.groovy.runtime.GStringImpl
import org.springframework.context.ApplicationContext

import java.lang.reflect.Type

class GsonService {

    Gson gson
    GsonBuilder builder = new GsonBuilder()

    def initialize(ApplicationContext ctx) {
        log.debug("Initializing gson service")
        builder.registerTypeAdapter(String.class, new StringSerializer())
        builder.registerTypeAdapter(GStringImpl.class, new GStringImplSerializer())
        builder.registerTypeAdapter(Integer.class, new IntegerSerializer())
        builder.registerTypeAdapter(Long.class, new LongSerializer())
        builder.registerTypeAdapter(Boolean.class, new BooleanSerializer())
        builder.registerTypeAdapter(ArrayList.class, new ArrayListSerializer(ctx))
        builder.registerTypeAdapter(LinkedHashMap.class, new LinkedHashMapSerializer(ctx))
        builder.registerTypeAdapter(HashSet.class, new HashSetSerializer(ctx))
        gson = builder.create()
    }

    String serializeAsJson(value) {
        //special case for nulls
        if (value == null) {
            JsonObject result = new JsonObject()
            result.addProperty("type", "nullField")
            return result
        }

        return gson.toJson(value)
    }

    def deserializeJson(JsonObject json) {
        String classType = json.get("type").getAsString()

        //handle null case
        if (classType == "nullField") {
            return null
        }

        Class objectClass = Class.forName(classType, true, Thread.currentThread().contextClassLoader)
        return gson.fromJson(json, objectClass)
    }

    public registerTypeAdapter(Type type, Object typeAdapter) {
        builder.registerTypeAdapter(type, typeAdapter)
        gson = builder.create()
    }

}
