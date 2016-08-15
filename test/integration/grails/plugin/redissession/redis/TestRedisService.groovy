package grails.plugin.redissession.redis

/**
 * Redis Testing Service which creates a service using {@link TestRedis} for usage in tests.
 *
 * @author Daniel Muehlbachler
 */
class TestRedisService {
    def redis

    TestRedisService(TestRedis redis) {
        this.redis = redis
    }

    def flushDB() {
        redis.flushDB()
    }

    def withRedis(Closure closure) {
        return closure(redis)
    }
}
