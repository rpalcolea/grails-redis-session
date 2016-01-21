package grails.plugin.redissession

import com.google.gson.GsonBuilder
import com.google.gson.Gson
import com.google.gson.JsonObject
import grails.plugin.redissession.serializers.*

import java.lang.reflect.Type

class GsonService {

    Gson gson
    GsonBuilder builder = new GsonBuilder()

    def initialize() {
        log.debug("Initializing gson service")
        builder.registerTypeAdapter(String.class, new StringSerializer())
        builder.registerTypeAdapter(Integer.class, new IntegerSerializer())
        builder.registerTypeAdapter(Long.class, new LongSerializer())
        builder.registerTypeAdapter(Boolean.class, new BooleanSerializer())
        builder.registerTypeAdapter(ArrayList.class, new ArrayListSerializer())
        builder.registerTypeAdapter(LinkedHashMap.class, new LinkedHashMapSerializer())
        builder.registerTypeAdapter(HashSet.class, new HashSetSerializer())
        gson = builder.create()
    }

    String serializeAsJson(value) {
        return gson.toJson(value)
    }

    def deserializeJson(JsonObject json) {
        Class objectClass = Class.forName(json.get("type").getAsString())
        return gson.fromJson(json, objectClass)
    }

    public registerTypeAdapter(Type type, Object typeAdapter) {
        builder.registerTypeAdapter(type, typeAdapter)
        gson = builder.create()
    }

}
