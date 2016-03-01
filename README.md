# Redis Database Session Grails Plugin

## Summary
Stores HTTP sessions in a Redis.

## Requirements
1. [Database Session Plugin](http://grails.org/plugin/database-session) 1.2.1+
2. [Redis Plugin](http://grails.org/plugin/redis) 1.5.2+

## Description

This plugin lets you store HTTP session data in a redis store using [Database Session Plugin](http://grails.org/plugin/database-session).

## Artifacts

This contains a RedisPersistentService.groovy with the persistence logic and RedisSessionCleanupService.groovy for cleanup sessions.

It registers two aliases inside the doWithSpring closure. With this, it can work with the existing Proxy Session Filter, SessionFilters (for flash scope) and DatabaseCleanupJob

```groovy
springConfig.addAlias 'gormPersisterService', 'redisPersistentService'
springConfig.addAlias 'databaseCleanupService', 'redisSessionCleanupService'
```

## Usage

For configuring, please refer to the [Database Session Plugin](http://grails.org/plugin/database-session) documents.

To set the session timeout to something other than the default of 30 minutes, set the `sessionTimeout` flag in your application's `config.groovy` as follows:

`grails.plugin.redisdatabasesession.sessionTimeout = 60`

## Json Serialization

By default objects are serialized as `byte[]`, but JSON serialization is also available.

To enable JSON serialization set the `useJson` flag to `true` in your application's `config.groovy` as follows:

`grails.plugin.redisdatabasesession.useJson = true`

To store the grails flash scope in the Redis, rather than the default of the request, set `storeFlashScopeWithRedis` to true as follows:

`grails.plugin.redisdatabasesession.storeFlashScopeWithRedis = true`

**Warning:** While serialization for some commonly needed classes has been taken care of, it is possible that you may need to implement your own type adapters depending on what you are storing in your session.

Custom type adapters will need to produce JSON that has both a `type` field and a `value` field. Additionally, the type adapter is a `gson` type adapter and must implement `com.google.gson.JsonSerializer` and/or `com.google.gson.JsonDeserializer`

* `type` should be the canonical name of the class for which you are registering a type adapter.
* `value` should be the data stored for the object.

An example of a type adapter for a Double is as follows:

```groovy
class DoubleSerializer implements JsonSerializer<Double>, JsonDeserializer<Double> {

    JsonElement serialize(Double aDouble, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject()
        result.addProperty("type", "java.lang.Double")
        result.addProperty("value", aDouble.toString())
        return result
    }
    
    Double deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject()
        return jsonObject.get("value").getAsDouble()
    }
}
```

The type adapter must then be registered with the `registerTypeAdapter` method on the plugin's `GsonService`.

The recommend place to register the type adapter is from your applications's `BootStrap.groovy`. To register our Double serializer, we would do as follows within `BootStrap.groovy`:

```groovy
def redisDatabaseSessionGsonService //autowires our plugin's GsonService

redisDatabaseSessionGsonService.registerTypeAdapter(Double.class, new DoubleSerializer())
```

## Authors
[Roberto Perez Alcolea](http://www.perezalcolea.info)

[Brit Indrelie](http://www.objectpartners.com)

Raj Govindarajan

