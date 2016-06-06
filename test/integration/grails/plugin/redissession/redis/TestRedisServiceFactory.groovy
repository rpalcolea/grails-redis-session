package grails.plugin.redissession.redis

/**
 * Redis Testing Service Factory which created a {@link TestRedisService} for usage in tests!
 *
 * @author Daniel Muehlbachler
 */
class TestRedisServiceFactory {

    public static TestRedisService getInstance() {
        return new TestRedisService(new TestRedis("localhost"))
    }

    private TestRedisServiceFactory() {}
}
