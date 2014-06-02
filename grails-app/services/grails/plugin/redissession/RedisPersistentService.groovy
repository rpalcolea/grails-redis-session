package grails.plugin.redissession

import grails.plugin.databasesession.InvalidatedSessionException
import grails.plugin.databasesession.Persister
import grails.plugin.databasesession.SessionProxyFilter
import grails.util.GrailsUtil
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import redis.clients.jedis.Jedis

/**
 * @author Roberto Perez
 * @author Raj Govindarajan
 */
class RedisPersistentService implements Persister  {
    static transactional = false
    def redisService
    def grailsApplication

    private static final String MAX_INACTIVE_INTERVAL = "maxInactiveInterval"
    private static final String INVALIDATED = "invalidated"
    private static final String SESSION_ATTRIBUTES_PREFIX = "session_attributes:"
    private static final String SESSION_PREFIX = "session:"
    protected static final String LAST_ACCESSED_TIME_ZSET = "sessions_last_accessed_times"

    void create(String sessionId) {
        try {
            redisService.withRedis { Jedis redis ->
                if (redis.exists("${SESSION_PREFIX}$sessionId")) {
                    return
                }

                def currentTime = System.currentTimeMillis()

                redis.hmset("${SESSION_PREFIX}${sessionId}", [creationTime: currentTime.toString(), (INVALIDATED): "false", (MAX_INACTIVE_INTERVAL): "30"])

                //Adding the session to a zset
                redis.zadd(LAST_ACCESSED_TIME_ZSET, currentTime, sessionId)


            }
        }
        catch (e) {
            handleException e
        }
    }

    Object getAttribute(String sessionId, String key) throws InvalidatedSessionException {
        if (key == null) return null

        if (GrailsApplicationAttributes.FLASH_SCOPE == key) {
            // special case; use request scope since a new deserialized instance is created each time it's retrieved from the session
            def fs = SessionProxyFilter.request.getAttribute(GrailsApplicationAttributes.FLASH_SCOPE)
            if (fs != null) {
                return fs
            }
        }

        try {
            def attribute

            redisService.withRedis { Jedis redis ->
                def sessionProperties = redis.hgetAll("${SESSION_PREFIX}${sessionId}")

                Long lastAccessedTime = redis.zscore(LAST_ACCESSED_TIME_ZSET, sessionId)?.toLong()

                checkInvalidated(sessionProperties.get(INVALIDATED), lastAccessedTime, sessionProperties.get(MAX_INACTIVE_INTERVAL))

                //Updating the session last accessed time in the zset
                redis.zadd(LAST_ACCESSED_TIME_ZSET, System.currentTimeMillis(), sessionId)

                def serializedAttribute = redis.hget((serialize("${SESSION_ATTRIBUTES_PREFIX}${sessionId}")), serialize(key))

                attribute = deserialize(serializedAttribute)

            }

            if (attribute != null && GrailsApplicationAttributes.FLASH_SCOPE == key) {
                SessionProxyFilter.request.setAttribute(GrailsApplicationAttributes.FLASH_SCOPE, attribute)
            }

            return attribute
        }
        catch (e) {
            handleException e
        }
    }

    void setAttribute(String sessionId, String key, Object value) throws InvalidatedSessionException {
        if (value == null) {
            removeAttribute sessionId, key
            return
        }

        // special case; use request scope and don't store in session, the filter will set it in the session at the end of the request
        if (value != null && GrailsApplicationAttributes.FLASH_SCOPE == key) {
            if (value != GrailsApplicationAttributes.FLASH_SCOPE) {
                SessionProxyFilter.request.setAttribute(GrailsApplicationAttributes.FLASH_SCOPE, value)
                return
            }

            // the filter set the value as the key, so retrieve it from the request
            value = SessionProxyFilter.request.getAttribute(GrailsApplicationAttributes.FLASH_SCOPE)
        }

        try {
            redisService.withRedis { Jedis redis ->
                def sessionProperties = redis.hgetAll("${SESSION_PREFIX}${sessionId}")

                Long lastAccessedTime = redis.zscore(LAST_ACCESSED_TIME_ZSET, sessionId)?.toLong()

                checkInvalidated(sessionProperties.get(INVALIDATED), lastAccessedTime, sessionProperties.get(MAX_INACTIVE_INTERVAL))

                //Updating the session last accessed time in the zset
                redis.zadd(LAST_ACCESSED_TIME_ZSET, System.currentTimeMillis(), sessionId)

                redis.hset(serialize("${SESSION_ATTRIBUTES_PREFIX}${sessionId}"), serialize(key), serialize(value))

            }
        }
        catch (e) {
            e.printStackTrace()
            handleException e
        }
    }

    void removeAttribute(String sessionId, String key) throws InvalidatedSessionException {
        if (key == null) return

        try {
            redisService.withRedis { Jedis redis ->
                if(redis.exists("${SESSION_ATTRIBUTES_PREFIX}${sessionId}"))
                    redis.hdel(serialize("${SESSION_ATTRIBUTES_PREFIX}${sessionId}"), serialize(key))
            }
        }
        catch (e) {
            e.printStackTrace()
            handleException e
        }
    }

    List<String> getAttributeNames(String sessionId) throws InvalidatedSessionException {
        try {
            List<String> result = []
            redisService.withRedis { Jedis redis ->
                if(redis.exists("${SESSION_ATTRIBUTES_PREFIX}${sessionId}"))
                    result = redis.hkeys((serialize("${SESSION_ATTRIBUTES_PREFIX}${sessionId}")))
            }
            return result.collect { deserialize(it) }
        }
        catch (e) {
            handleException e
        }
    }

    void invalidate(String sessionId) {
        try {

            def conf = grailsApplication.config.grails.plugin.databasesession
            def deleteInvalidSessions = conf.deleteInvalidSessions ?: false
            redisService.withRedis { Jedis redis ->
                if (deleteInvalidSessions) {
                    redis.del("${SESSION_PREFIX}${sessionId}")
                    redis.del(serialize("${SESSION_ATTRIBUTES_PREFIX}${sessionId}"))
                    //Remove the session from the zset
                    redis.zrem(LAST_ACCESSED_TIME_ZSET, sessionId)
                } else {
                    redis.hset("${SESSION_PREFIX}$sessionId", INVALIDATED, "true")
                }
            }
        }
        catch (e) {
            e.printStackTrace()
            handleException e
        }
    }

    long getLastAccessedTime(String sessionId) throws InvalidatedSessionException {
        Long lastAccessedTime  = null
        redisService.withRedis { Jedis redis ->
            def sessionProperties = redis.hgetAll("${SESSION_PREFIX}${sessionId}")
            lastAccessedTime = redis.zscore(LAST_ACCESSED_TIME_ZSET, sessionId)?.toLong()
            checkInvalidated(sessionProperties.get(INVALIDATED), lastAccessedTime, sessionProperties.get(MAX_INACTIVE_INTERVAL))
        }
        return lastAccessedTime
    }

    void setMaxInactiveInterval(String sessionId, int interval) throws InvalidatedSessionException {
        redisService.withRedis { Jedis redis ->

            def sessionProperties = redis.hgetAll("${SESSION_PREFIX}${sessionId}")
            Long lastAccessedTime = redis.zscore(LAST_ACCESSED_TIME_ZSET, sessionId)?.toLong()
            checkInvalidated(sessionProperties.get(INVALIDATED), lastAccessedTime, sessionProperties.get(MAX_INACTIVE_INTERVAL))
            if (interval == 0) {
                invalidate(sessionId)
                return
            }
            redis.hset("${SESSION_PREFIX}$sessionId", MAX_INACTIVE_INTERVAL, interval.toString())
        }
    }

    int getMaxInactiveInterval(String sessionId) throws InvalidatedSessionException {
        int maxInterval
        redisService.withRedis { Jedis redis ->
            def sessionProperties = redis.hgetAll("${SESSION_PREFIX}${sessionId}")
            Long lastAccessedTime = redis.zscore(LAST_ACCESSED_TIME_ZSET, sessionId)?.toLong()
            checkInvalidated(sessionProperties.get(INVALIDATED), lastAccessedTime, sessionProperties.get(MAX_INACTIVE_INTERVAL))
            maxInterval = sessionProperties.get(MAX_INACTIVE_INTERVAL)
        }
        return maxInterval
    }

    boolean checkInvalidated(def invalidated, def lastAccessedTime, def maxInactiveInterval) {
        boolean result = _isValid(invalidated, lastAccessedTime, maxInactiveInterval)
        if(!result)
            throw new InvalidatedSessionException()
        return result
    }


    boolean isValid(String sessionId) {
        redisService.withRedis { Jedis redis ->
            def sessionProperties = redis.hgetAll("${SESSION_PREFIX}${sessionId}")
            Long lastAccessedTime = redis.zscore(LAST_ACCESSED_TIME_ZSET, sessionId)?.toLong()
            if(sessionProperties && lastAccessedTime)
                return _isValid(sessionProperties.get(INVALIDATED), lastAccessedTime, sessionProperties.get(MAX_INACTIVE_INTERVAL))
            else
                return false
        }
    }

    private boolean _isValid(def invalidated, Long lastAccessedTime, def maxInactiveInterval) {
        invalidated = invalidated?.toBoolean()
        maxInactiveInterval = maxInactiveInterval?.toLong()
        boolean notExpired = lastAccessedTime > System.currentTimeMillis() - maxInactiveInterval * 1000 * 60
        return (invalidated == false && notExpired)
    }


    protected void handleException(e) {
        if (e instanceof InvalidatedSessionException) {
            throw e
        }
        GrailsUtil.deepSanitize e
        log.error e.message, e
    }

    public deserialize(byte[] serialized) {
        if (!serialized) {
            return null
        }

        new ObjectInputStream(new ByteArrayInputStream(serialized)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
                Class.forName objectStreamClass.name, true, Thread.currentThread().contextClassLoader
            }
        }.readObject()
    }

    public byte[] serialize(value) {
        if (value == null) {
            return null
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream()

        new ObjectOutputStream(baos).writeObject value
        baos.toByteArray()
    }
}
