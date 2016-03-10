package grails.plugin.redissession
import redis.clients.jedis.Jedis

/**
 * @author Roberto Perez
 * @author Raj Govindarajan
 */
class RedisSessionCleanupService {
    def transactional = false

    def grailsApplication

    def redisPersistentService
    /**
     * Delete PersistentSessions where the last accessed time is older than a cutoff value.
     */
    void cleanup(Boolean removeAllSessions = false) {
        def config = grailsApplication.config.grails.plugin.databasesession

        float maxAge = (config.cleanup.maxAge ?: 30) as Float

        long age = System.currentTimeMillis() - maxAge * 1000 * 60

        Set<String> expiredSessions = []

        redisPersistentService.redisService.withRedis { Jedis redis ->
            if (removeAllSessions) {
                expiredSessions = redis.zrangeByScore(redisPersistentService.LAST_ACCESSED_TIME_ZSET, 0, System.currentTimeMillis())
            } else {
                expiredSessions = redis.zrangeByScore(redisPersistentService.LAST_ACCESSED_TIME_ZSET, 0, age)
            }
        }

        expiredSessions.each { expiredSession ->
            try {
                redisPersistentService.invalidate(expiredSession)
            } catch (e) {
                log.error("Couldn't invalidate session ${expiredSession}")
            }
        }
    }
}
