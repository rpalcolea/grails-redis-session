import grails.plugin.redissession.RedisPersistentService
import grails.plugin.redissession.RedisSessionCleanupService
import grails.plugin.databasesession.SessionProxyFilter
import org.codehaus.groovy.grails.web.servlet.mvc.SynchronizerTokensHolder

import javax.servlet.http.HttpSession

class RedisDatabaseSessionGrailsPlugin {
    def version = "1.2.1-RC8"
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
    def developers = [[name: "Raj Govindarajan", email: ""],[name: "Brit Indrelie", email: "brit.indrelie@objectpartners.com"]]
    def issueManagement = [system: "Github", url: "https://github.com/rpalcolea/grails-redis-session/issues"]
    def scm = [url: "https://github.com/rpalcolea/grails-redis-session/"]


    def doWithSpring = {
        springConfig.addAlias 'gormPersisterService', 'redisPersistentService'
        springConfig.addAlias 'databaseCleanupService', 'redisSessionCleanupService'
    }

    def doWithApplicationContext = { applicationContext ->
        if (useJson(application.config)) {
            applicationContext.gsonService.initialize(applicationContext)
        }

        //SynchronizerTokensHolder doesn't explicitly save the holder to the session when generating a token.
        //This causes the synchronizer token to not be set in form tag lib, meaning withForm closures didn't work.
        SynchronizerTokensHolder.metaClass.sessionId = null

        SynchronizerTokensHolder.metaClass.'static'.store = { HttpSession httpSession ->
            SynchronizerTokensHolder tokensHolder = httpSession.getAttribute(SynchronizerTokensHolder.HOLDER)
            if (!tokensHolder) {
                tokensHolder = new SynchronizerTokensHolder()
                httpSession.setAttribute(SynchronizerTokensHolder.HOLDER, tokensHolder)
            }

            tokensHolder.sessionId = httpSession.id

            return tokensHolder
        }

        SynchronizerTokensHolder.metaClass.generateToken = { String url ->

            final UUID uuid = UUID.randomUUID()

            getTokens(url).add(uuid)

            applicationContext.redisPersistentService.setAttribute(sessionId, SynchronizerTokensHolder.HOLDER, delegate)
            return uuid.toString()
        }

        SynchronizerTokensHolder.metaClass.resetToken = { String url ->
            currentTokens.remove(url)
            applicationContext.redisPersistentService.setAttribute(sessionId, SynchronizerTokensHolder.HOLDER, delegate)
        }

        SynchronizerTokensHolder.metaClass.resetToken = { String url, String token ->
            if (url && token) {
                final Set set = getTokens(url)
                try {
                    set.remove(UUID.fromString(token))
                }
                catch (IllegalArgumentException ignored) {}
                if (set.isEmpty()) {
                    currentTokens.remove(url)
                }
            }
            applicationContext.redisPersistentService.setAttribute(sessionId, SynchronizerTokensHolder.HOLDER, delegate)
        }
    }

    private boolean useJson(config) {
        def enabled = config.grails.plugin.redisdatabasesession.useJson
        enabled instanceof Boolean ?: false
    }

}
