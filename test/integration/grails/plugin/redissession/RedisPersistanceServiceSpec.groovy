package grails.plugin.redissession

import grails.plugin.redissession.redis.TestRedisServiceFactory
import grails.test.spock.IntegrationSpec

/**
 * RedisPersistanceService tests
 *
 * @author Daniel Muehlbachler
 */
class RedisPersistanceServiceSpec extends IntegrationSpec {

    private String SESSION_ID = "sessionId"

    def redisService

    def setup() {
        redisService = TestRedisServiceFactory.getInstance()
        applicationContext.redisPersistentService.redisService = redisService
    }

    def cleanup() {
        redisService.flushDB()
        applicationContext.grailsApplication.config.grails.plugin.redisdatabasesession.sessionTimeout = null
    }

    void "test create default session"() {
        given:
        String sessionId = "default:$SESSION_ID"

        when:
        applicationContext.redisPersistentService.create(sessionId)

        then:
        applicationContext.redisPersistentService.getMaxInactiveInterval(sessionId) == RedisPersistentService.DEFAULT_MAX_INACTIVE_INTERVAL
    }

    void "test create session with config sessionTimeout"() {
        given:
        String sessionId = "configTimeout:$SESSION_ID"
        int sessionTimeout = 60
        applicationContext.grailsApplication.config.grails.plugin.redisdatabasesession.sessionTimeout = sessionTimeout

        when:
        applicationContext.redisPersistentService.create(sessionId)

        then:
        applicationContext.redisPersistentService.getMaxInactiveInterval(sessionId) == sessionTimeout
    }

    void "test set inactive interval session"() {
        given:
        String sessionId = "setTimeout:$SESSION_ID"
        int sessionTimeout = 45
        applicationContext.redisPersistentService.create(sessionId)

        when:
        applicationContext.redisPersistentService.setMaxInactiveInterval(sessionId, sessionTimeout)

        then:
        applicationContext.redisPersistentService.getMaxInactiveInterval(sessionId) == sessionTimeout
    }

    void "test setAttribute and getAttribute"() {
        given:
        applicationContext.redisPersistentService.create(SESSION_ID)
        String key = 'key'
        String value = 'value'

        when:
        applicationContext.redisPersistentService.setAttribute(SESSION_ID, key, value)

        then:
        applicationContext.redisPersistentService.getAttribute(SESSION_ID, key) == value
    }

    void "test getAttributeNames"() {
        given:
        applicationContext.redisPersistentService.create(SESSION_ID)
        String key = 'key'
        String value = 'value'

        when:
        applicationContext.redisPersistentService.setAttribute(SESSION_ID, key, value)
        List<String> keys = applicationContext.redisPersistentService.getAttributeNames(SESSION_ID)

        then:
        keys.size() == 1
        keys.contains(key)
    }

    void "test removeAttribute"() {
        given:
        applicationContext.redisPersistentService.create(SESSION_ID)
        String key = 'key'
        String value = 'value'

        when:
        applicationContext.redisPersistentService.setAttribute(SESSION_ID, key, value)
        String storedValue = applicationContext.redisPersistentService.getAttribute(SESSION_ID, key)
        applicationContext.redisPersistentService.removeAttribute(SESSION_ID, key)

        then:
        storedValue == value
        applicationContext.redisPersistentService.getAttribute(SESSION_ID, key) == null
    }
}

