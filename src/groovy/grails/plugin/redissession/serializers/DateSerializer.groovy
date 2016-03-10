package grails.plugin.redissession.serializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer

import java.lang.reflect.Type
import java.text.DateFormat
import java.text.SimpleDateFormat

class DateSerializer implements JsonSerializer<Date>, JsonDeserializer<Date> {

    DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")

    JsonElement serialize(Date date, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject()
        result.addProperty("type", "java.util.Date")
        result.addProperty("value", format.format(date))
        return result
    }

    Date deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        JsonObject jobject = json.getAsJsonObject()
        String dateString = jobject.get("value").getAsString()

        return format.parse(dateString)
    }

}