package grails.plugin.redissession

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import grails.test.spock.IntegrationSpec
import groovy.json.StringEscapeUtils
import java.lang.reflect.Type
import spock.lang.Unroll


class GsonServiceSpec extends IntegrationSpec {

    def gsonService

    def setup() {
        gsonService.initialize()
    }

    def cleanup() {
    }

    @Unroll
    void "test serializeAsJson with value to serialize = #value"() {
        given:
        def result = gsonService.serializeAsJson(value)

        expect:
        StringEscapeUtils.unescapeJava(result) == expected

        where:
        value                           | expected
        "Test string"                   | '''{"type":"java.lang.String","value":"Test string"}'''
        2l                              | '''{"type":"java.lang.Long","value":2}'''
        5                               | '''{"type":"java.lang.Integer","value":5}'''
        false                           | '''{"type":"java.lang.Boolean","value":false}'''
        [1,"b", 2l, false]              | '''{"type":"java.util.ArrayList","value":"["{\\"type\\":\\"java.lang.Integer\\",\\"value\\":1}","{\\"type\\":\\"java.lang.String\\",\\"value\\":\\"b\\"}","{\\"type\\":\\"java.lang.Long\\",\\"value\\":2}","{\\"type\\":\\"java.lang.Boolean\\",\\"value\\":false}"]"}'''
        [1:"test", "two": 2, 3l: false] | '''{"type":"java.util.LinkedHashMap","value":"{"{\\"type\\":\\"java.lang.Integer\\",\\"value\\":1}":"{\\"type\\":\\"java.lang.String\\",\\"value\\":\\"test\\"}","{\\"type\\":\\"java.lang.String\\",\\"value\\":\\"two\\"}":"{\\"type\\":\\"java.lang.Integer\\",\\"value\\":2}","{\\"type\\":\\"java.lang.Long\\",\\"value\\":3}":"{\\"type\\":\\"java.lang.Boolean\\",\\"value\\":false}"}"}'''
    }

    @Unroll
    void "test deserializeJson for class #type"() {
        given:
        JsonObject jsonObject = new JsonObject()
        jsonObject.addProperty("type", type)
        jsonObject.addProperty("value", value)
        def result = gsonService.deserializeJson(jsonObject)

        expect:
        result == expected

        where:
        type                        | value                                             | expected
        "java.lang.String"          | "Test String"                                     | "Test String"
        "java.lang.Long"            | 2l                                                | 2l
        "java.lang.Integer"         | 5                                                 | 5
        "java.lang.Boolean"         | false                                             | false
        "java.util.ArrayList"       | buildJsonArray([1, "b", 2l, false])               | [1, "b", 2l, false]
        "java.util.LinkedHashMap"   | buildJsonHashMap([1:"test", "two": 2, 3l: false]) | [1: "test", "two": 2, 3l:false]
    }

    void "test register type adapter"() {
        given:
        gsonService.registerTypeAdapter(Double.class, new TestDoubleSerializer())
        String serializedDouble = gsonService.serializeAsJson(2 as Double)
        //ensure other serializers are still working
        String serializedString = gsonService.serializeAsJson("Test string")


        expect:
        serializedDouble == '''{"value":"Double test serialization"}'''
        serializedString == '''{"type":"java.lang.String","value":"Test string"}'''


    }

    private buildJsonArray(ArrayList arrayList) {
        JsonArray arrayJson = new JsonArray()
        arrayList.each {
            String itemJson = gsonService.serializeAsJson(it)
            JsonPrimitive jsonPrimitive = new JsonPrimitive(itemJson)
            arrayJson.add(jsonPrimitive)
        }
        return arrayJson.toString()
    }

    private buildJsonHashMap(LinkedHashMap map) {
        JsonObject valObject = new JsonObject()
        map.each {
            valObject.addProperty(gsonService.serializeAsJson(it.key), gsonService.serializeAsJson(it.value))
        }
        return valObject.toString()
    }
}

class TestDoubleSerializer implements JsonSerializer<Double> {

    JsonElement serialize(Double aDouble, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject()
        result.addProperty("value", "Double test serialization")
        return result
    }
}
