package grails.plugin.redissession

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import grails.test.spock.IntegrationSpec
import grails.util.Holders
import groovy.json.StringEscapeUtils
import org.codehaus.groovy.grails.web.servlet.GrailsFlashScope
import org.codehaus.groovy.grails.web.servlet.mvc.SynchronizerTokensHolder

import java.lang.reflect.Type
import spock.lang.Unroll


class GsonServiceSpec extends IntegrationSpec {

    def gsonService

    def setup() {
        gsonService.initialize(Holders.getApplicationContext())
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
        value                                                       | expected
        "Test string"                                               | '''{"type":"java.lang.String","value":"Test string"}'''
        UUID.fromString("2008424f-8d17-41f2-bcbc-3395f29c6170")     | '''{"type":"java.util.UUID","value":"2008424f-8d17-41f2-bcbc-3395f29c6170"}'''
        "Test ${"gString"}"                                         | '''{"type":"java.lang.String","value":"Test gString"}'''
        2l                                                          | '''{"type":"java.lang.Long","value":2}'''
        5                                                           | '''{"type":"java.lang.Integer","value":5}'''
        false                                                       | '''{"type":"java.lang.Boolean","value":false}'''
        null                                                        | '''{"type":"nullField"}'''
        [1,"b", 2l, false, null]                                    | '''{"type":"java.util.ArrayList","value":"["{\\"type\\":\\"java.lang.Integer\\",\\"value\\":1}","{\\"type\\":\\"java.lang.String\\",\\"value\\":\\"b\\"}","{\\"type\\":\\"java.lang.Long\\",\\"value\\":2}","{\\"type\\":\\"java.lang.Boolean\\",\\"value\\":false}","{\\"type\\":\\"nullField\\"}"]"}'''
        [1:"test", "two": 2, 3l: false, "arrayVal": ["one",2]]      | '''{"type":"java.util.LinkedHashMap","value":"{"{\\"type\\":\\"java.lang.Integer\\",\\"value\\":1}":"{\\"type\\":\\"java.lang.String\\",\\"value\\":\\"test\\"}","{\\"type\\":\\"java.lang.String\\",\\"value\\":\\"two\\"}":"{\\"type\\":\\"java.lang.Integer\\",\\"value\\":2}","{\\"type\\":\\"java.lang.Long\\",\\"value\\":3}":"{\\"type\\":\\"java.lang.Boolean\\",\\"value\\":false}","{\\"type\\":\\"java.lang.String\\",\\"value\\":\\"arrayVal\\"}":"{\\"type\\":\\"java.util.ArrayList\\",\\"value\\":\\"[\\\\\\"{\\\\\\\\\\\\\\"type\\\\\\\\\\\\\\":\\\\\\\\\\\\\\"java.lang.String\\\\\\\\\\\\\\",\\\\\\\\\\\\\\"value\\\\\\\\\\\\\\":\\\\\\\\\\\\\\"one\\\\\\\\\\\\\\"}\\\\\\",\\\\\\"{\\\\\\\\\\\\\\"type\\\\\\\\\\\\\\":\\\\\\\\\\\\\\"java.lang.Integer\\\\\\\\\\\\\\",\\\\\\\\\\\\\\"value\\\\\\\\\\\\\\":2}\\\\\\"]\\"}"}"}'''
        new HashSet([1,"two", 2l, false])                           | '''{"type":"java.util.HashSet","value":"["{\\"type\\":\\"java.lang.Boolean\\",\\"value\\":false}","{\\"type\\":\\"java.lang.String\\",\\"value\\":\\"two\\"}","{\\"type\\":\\"java.lang.Integer\\",\\"value\\":1}","{\\"type\\":\\"java.lang.Long\\",\\"value\\":2}"]"}'''
        new SynchronizerTokensHolder()                              | '''{"type":"org.codehaus.groovy.grails.web.servlet.mvc.SynchronizerTokensHolder","value":"{"currentTokens":{}}"}'''
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
        type                        | value                                                                     | expected
        "java.lang.String"          | "Test String"                                                             | "Test String"
        "java.util.UUID"            | "2008424f-8d17-41f2-bcbc-3395f29c6170"                                    | UUID.fromString("2008424f-8d17-41f2-bcbc-3395f29c6170")
        "java.lang.Long"            | 2l                                                                        | 2l
        "java.lang.Integer"         | 5                                                                         | 5
        "java.lang.Boolean"         | false                                                                     | false
        "java.util.ArrayList"       | buildJsonArray([1, "b", 2l, false, null])                                 | [1, "b", 2l, false, null]
        "java.util.LinkedHashMap"   | buildJsonHashMap([1:"test", "two": 2, 3l: false, "arrayVal": ["one",2]])  | [1: "test", "two": 2, 3l:false, "arrayVal": ["one",2]]
        "java.util.HashSet"         | buildJsonArray([1,"two", 2l, false])                                      | new HashSet([1,"two", 2l, false])
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

    void "test serialize flash scope"() {

        when:
        def flashScope = new GrailsFlashScope()
        flashScope.put("numberMessagesShown", [10])
        def result = gsonService.serializeAsJson(flashScope)

        then:
        //nested json object strings == string escape hell
        result == '''{"type":"org.codehaus.groovy.grails.web.servlet.GrailsFlashScope","value":"{\\"type\\":\\"java.util.LinkedHashMap\\",\\"value\\":\\"{\\\\\\"{\\\\\\\\\\\\\\"type\\\\\\\\\\\\\\":\\\\\\\\\\\\\\"java.lang.String\\\\\\\\\\\\\\",\\\\\\\\\\\\\\"value\\\\\\\\\\\\\\":\\\\\\\\\\\\\\"numberMessagesShown\\\\\\\\\\\\\\"}\\\\\\":\\\\\\"{\\\\\\\\\\\\\\"type\\\\\\\\\\\\\\":\\\\\\\\\\\\\\"java.util.ArrayList\\\\\\\\\\\\\\",\\\\\\\\\\\\\\"value\\\\\\\\\\\\\\":\\\\\\\\\\\\\\"[\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"type\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"java.lang.Integer\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\",\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"value\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":10}\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"]\\\\\\\\\\\\\\"}\\\\\\"}\\"}"}'''
    }

    void "test deserialize flash scope"() {
        when:
        JsonParser jsonParser = gsonService.getJsonParser()
        def flashScopeJson = jsonParser.parse('''{"type":"org.codehaus.groovy.grails.web.servlet.GrailsFlashScope","value":"{\\"type\\":\\"java.util.LinkedHashMap\\",\\"value\\":\\"{\\\\\\"{\\\\\\\\\\\\\\"type\\\\\\\\\\\\\\":\\\\\\\\\\\\\\"java.lang.String\\\\\\\\\\\\\\",\\\\\\\\\\\\\\"value\\\\\\\\\\\\\\":\\\\\\\\\\\\\\"numberMessagesShown\\\\\\\\\\\\\\"}\\\\\\":\\\\\\"{\\\\\\\\\\\\\\"type\\\\\\\\\\\\\\":\\\\\\\\\\\\\\"java.util.ArrayList\\\\\\\\\\\\\\",\\\\\\\\\\\\\\"value\\\\\\\\\\\\\\":\\\\\\\\\\\\\\"[\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"type\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"java.lang.Integer\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\",\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"value\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":10}\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"]\\\\\\\\\\\\\\"}\\\\\\"}\\"}"}''').getAsJsonObject()
        def deserialized = gsonService.deserializeJson(flashScopeJson)

        then:
        deserialized.getClass() == GrailsFlashScope.class
        deserialized.get("numberMessagesShown") == [10]

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
