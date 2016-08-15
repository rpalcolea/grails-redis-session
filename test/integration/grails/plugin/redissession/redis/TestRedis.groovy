package grails.plugin.redissession.redis

import redis.clients.jedis.Jedis

/**
 * Redis Testing Instance which ONLY overwrites NEEDED methods!
 *
 * @author Daniel Muehlbachler
 */
class TestRedis extends Jedis {
    Map<Object, Map> contents = [:]

    public TestRedis(String host) {
        super(host)
    }

    @Override
    String flushDB() {
        return contents.clear()
    }

    @Override
    String hmset(String key, Map<String, String> hash) {
        return contents.put(key, hash)
    }

    @Override
    Long zadd(String key, double score, String member) {
        Map members = contents.get(key)
        if (!members) {
            members = [:]
            contents.put(key, members)
        }
        members.put(member, score)
        return 1
    }

    @Override
    Double zscore(String key, String member) {
        return contents.get(key)?.get(member)
    }

    @Override
    Map<String, String> hgetAll(String key) {
        return contents.get(key)
    }

    @Override
    byte[] hget(byte[] key, byte[] field) {
        return contents.get(key.toList())?.get(field.toList())
    }

    @Override
    Long hset(String key, String field, String value) {
        return setFieldValue(key, field, value)
    }

    @Override
    Long hset(byte[] key, byte[] field, byte[] value) {
        return setFieldValue(key.toList(), field.toList(), value)
    }

    private Long setFieldValue(def key, def field, def value) {
        Map fields = contents.get(key)
        if (!fields) {
            fields = [:]
            contents.put(key, fields)
        }
        fields.put(field, value)
        return 1
    }

    @Override
    Long hdel(byte[] key, byte[] ... fields) {
        Map contentFields = contents.get(key.toList())
        if (contentFields) {
            fields.each {
                contentFields.remove(it.toList())
            }
        }
        return 1
    }

    @Override
    Set<byte[]> hkeys(byte[] key) {
        return contents.get(key.toList())?.keySet().collect { it as byte[] }
    }

    @Override
    Boolean exists(String key) {
        return contents.containsKey(key)
    }

    @Override
    Boolean exists(byte[] key) {
        return contents.containsKey(key.toList())
    }

    @Override
    Long del(String key) {
        contents.remove(key)
        return 1
    }

    @Override
    Long del(byte[] key) {
        contents.remove(key.toList())
        return 1
    }

    @Override
    Long zrem(String key, String... members) {
        Map contentMembers = contents.get(key)
        if (contentMembers) {
            members.each {
                contentMembers.remove(it)
            }
        }
        return 1
    }
}
