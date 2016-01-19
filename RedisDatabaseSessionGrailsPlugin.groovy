import grails.plugin.redissession.RedisPersistentService
import grails.plugin.redissession.RedisSessionCleanupService
import grails.plugin.databasesession.SessionProxyFilter

class RedisDatabaseSessionGrailsPlugin {
    def version = "1.2"
    def grailsVersion = "2.0 > *"
    def loadAfter = ["database-session", "redis"]
    def dependsOn = ["database-session":"1.2.1 > *", "redis":"1.5.2 > *"]

    def title = "Redis Session Plugin" // Headline display name of the plugin
    def author = "Roberto Perez Alcolea"
    def authorEmail = "roberto@perezalcolea.info"
    def description = '''\
Stores HTTP sessions in a Redis data store.
'''
    def documentation = "http://grails.org/plugin/grails-redis-session"
    def license = "APACHE"

    def organization = [name: "TollFreeForwarding", url: "http://www.tollfreeforwarding.com/"]
    def developers = [[name: "Raj Govindarajan", email: ""]]
    def issueManagement = [system: "Github", url: "https://github.com/rpalcolea/grails-redis-session/issues"]
    def scm = [url: "https://github.com/rpalcolea/grails-redis-session/"]


    def doWithSpring = {
        springConfig.addAlias 'gormPersisterService', 'redisPersistentService'
        springConfig.addAlias 'databaseCleanupService', 'redisSessionCleanupService'
    }

    def doWithApplicationContext = { applicationContext ->
        if (useJson(application.config)) {
            applicationContext.gsonService.initialize()
        }
    }

    private boolean useJson(config) {
        def enabled = config.grails.plugin.redisdatabasesession.usejson
        enabled instanceof Boolean ?: false
    }

}
