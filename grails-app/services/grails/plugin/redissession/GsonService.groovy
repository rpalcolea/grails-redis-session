package grails.plugin.redissession

import com.google.gson.GsonBuilder
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import grails.plugin.redissession.serializers.*
import org.codehaus.groovy.grails.web.servlet.GrailsFlashScope
import org.codehaus.groovy.grails.web.servlet.mvc.SynchronizerTokensHolder
import org.codehaus.groovy.runtime.GStringImpl
import org.springframework.context.ApplicationContext

import java.lang.reflect.Type

class GsonService {
    static transactional = false

    Gson gson
    GsonBuilder builder = new GsonBuilder()
    JsonParser jsonParser = new JsonParser()

    def initialize(ApplicationContext ctx) {
        log.debug("Initializing gson service")
        builder.registerTypeAdapter(String.class, new StringSerializer())
        builder.registerTypeAdapter(UUID.class, new UuidSerializer())
        builder.registerTypeAdapter(GStringImpl.class, new GStringImplSerializer())
        builder.registerTypeAdapter(Integer.class, new IntegerSerializer())
        builder.registerTypeAdapter(Long.class, new LongSerializer())
        builder.registerTypeAdapter(Boolean.class, new BooleanSerializer())
        builder.registerTypeAdapter(Date.class, new DateSerializer())
        builder.registerTypeAdapter(ArrayList.class, new ArrayListSerializer(ctx))
        builder.registerTypeAdapter(LinkedHashMap.class, new LinkedHashMapSerializer(ctx))
        builder.registerTypeAdapter(HashSet.class, new HashSetSerializer(ctx))
        builder.registerTypeAdapter(LinkedHashSet.class, new LinkedHashSetSerializer(ctx))
        builder.registerTypeAdapter(ConfigObject.class, new ConfigObjectSerializer(ctx))
        builder.registerTypeAdapter(GrailsFlashScope.class, new GrailsFlashScopeSerializer(ctx))
        builder.registerTypeAdapter(RedisGrailsFlashScope.class, new GrailsFlashScopeSerializer(ctx))
        builder.registerTypeAdapter(SynchronizerTokensHolder.class, new SynchronizerTokensHolderSerializer(ctx))
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
